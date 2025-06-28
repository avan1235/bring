package `in`.procyk.bring.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import `in`.procyk.bring.ShoppingListData
import `in`.procyk.bring.ShoppingListItemData
import `in`.procyk.bring.ShoppingListItemData.CheckedStatusData.Unchecked
import `in`.procyk.bring.UserShoppingListSuggestionsData
import `in`.procyk.bring.db.Database
import `in`.procyk.bring.db.ShoppingListEntity
import `in`.procyk.bring.db.ShoppingListItemEntity
import `in`.procyk.bring.db.ShoppingListItemsTable
import `in`.procyk.bring.extract.*
import `in`.procyk.bring.service.ShoppingListService.*
import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class ShoppingListServiceImpl(
    override val coroutineContext: CoroutineContext,
    private val application: Application,
    private val sessions: ConcurrentHashMap<Uuid, Flow<Either<ShoppingListData, GetShoppingListError>?>>,
) : ShoppingListService {

    private val extractor: IngredientsExtractor = AggregateIngredientsExtractor(
        AniaGotujeIngredientsExtractor,
        KwestiaSmakuIngredientsExtractor,
        CookidooIngredientsExtractor,
    )

    override suspend fun createNewShoppingList(
        userId: Uuid,
        input: String,
    ): Either<Uuid, CreateNewShoppingListError> {
        if (input.isBlank()) {
            return CreateNewShoppingListError.InvalidName.right()
        }
        val (title, ingredients) = when {
            !extractor.supports(input) -> input to emptyList()
            else -> runCatching {
                val document = Ksoup.parseGetRequest(input)
                val title = extractor.extractTitle(document)
                    ?: return CreateNewShoppingListError.ExtractionError.right()
                val ingredients = extractor.extractIngredients(document)
                    .map { it.description }
                    .takeIf { it.isNotEmpty() }
                    ?: return CreateNewShoppingListError.ExtractionError.right()
                title to ingredients
            }.getOrNull() ?: return CreateNewShoppingListError.ExtractionError.right()
        }
        return runNewSuspendedTransactionCatchingAs(CreateNewShoppingListError.Internal) {
            val userUUID = userId.toJavaUuid()
            val listId = ShoppingListEntity.new {
                this.name = title
                this.byUserId = userUUID
                this.createdAt = Clock.System.now()
            }.id
            addEntriesToShoppingListInTransaction(userUUID, listId, ingredients)
            listId.value.toKotlinUuid().left()
        }
    }

    override suspend fun removeShoppingListItem(
        itemId: Uuid,
    ): Either<Unit, RemoveShoppingListItemError> {
        val itemUUID = itemId.toJavaUuid()
        return runNewSuspendedTransactionCatchingAs(RemoveShoppingListItemError.Internal) {
            val removed = ShoppingListItemsTable.deleteWhere {
                ShoppingListItemsTable.id eq itemUUID
            } > 0
            when {
                removed -> Unit.left()
                else -> RemoveShoppingListItemError.UnknownItemId.right()
            }
        }
    }

    override fun getShoppingList(
        listId: Uuid,
    ): Flow<Either<ShoppingListData, GetShoppingListError>> = sessions.getOrPut(listId) {
        channelFlow {
            val channelName = "event_${listId.toHexDashString().replace('-', '_')}"
            while (currentCoroutineContext().isActive) {
                Database.createListener(channelName) { payload ->
                    trySendBlocking(payload)
                }.use {
                    delay(30.minutes)
                }
            }
            awaitClose()
        }.onStart {
            reorderListItems(listId).onRight { cancel(it.name) }
        }.map {
            getShoppingListData(listId)
        }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = application,
                started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
                initialValue = null,
            )
            .onSubscription {
                emit(getShoppingListData(listId))
            }
    }.filterNotNull()

    private suspend fun getShoppingListData(
        listId: Uuid,
    ): Either<ShoppingListData, GetShoppingListError> {
        return runNewSuspendedTransactionCatchingAs(GetShoppingListError.Internal) txn@{
            val list = ShoppingListEntity.findById(listId.toJavaUuid())
                ?: return@txn GetShoppingListError.UnknownListId.right()
            ShoppingListData(
                id = list.id.value.toKotlinUuid(),
                name = list.name,
                byUserId = list.byUserId.toKotlinUuid(),
                createdAt = list.createdAt,
                items = list.items.map { item ->
                    ShoppingListItemData(
                        id = item.id.value.toKotlinUuid(),
                        name = item.name,
                        byUserId = item.byUserId.toKotlinUuid(),
                        createdAt = item.createdAt,
                        order = item.order,
                        status = item.status
                    )
                }
            ).left()
        }
    }

    override suspend fun getUserShoppingListSuggestions(
        userId: Uuid,
        limit: Int,
    ): Either<UserShoppingListSuggestionsData, GetUserShoppingListSuggestionsError> {
        return runNewSuspendedTransactionCatchingAs(GetUserShoppingListSuggestionsError.Internal) {
            val count = ShoppingListItemsTable.name.count().alias("count")
            ShoppingListItemsTable.select(
                ShoppingListItemsTable.name, count,
            ).where {
                ShoppingListItemsTable.byUserId eq userId.toJavaUuid()
            }
                .groupBy(ShoppingListItemsTable.name)
                .orderBy(count, SortOrder.DESC)
                .limit(maxOf(limit, 0))
                .mapLazy { it[ShoppingListItemsTable.name] }
                .toSet()
                .let(::UserShoppingListSuggestionsData)
                .left()
        }
    }

    override suspend fun reorderListItems(
        listId: Uuid,
    ): Either<Unit, ReorderListError> {
        return runNewSuspendedTransactionCatchingAs(ReorderListError.Internal) {
            val conn = TransactionManager.current().connection
            val statement = conn.prepareStatement(
                sql = """
                    WITH numbered_rows
                             AS (SELECT *, row_number() OVER (ORDER BY "order") AS rn
                                 FROM shopping_list_items
                                 WHERE list_id = '$listId')
                    UPDATE shopping_list_items
                    SET "order" = (SELECT rn - 1.0
                                   FROM numbered_rows
                                   WHERE shopping_list_items.id = numbered_rows.id)
                    WHERE list_id = '$listId';                
                    """,
                returnKeys = false
            )
            statement.executeUpdate()
            Unit.left()
        }
    }

    override suspend fun addEntryToShoppingList(
        userId: Uuid,
        listId: Uuid,
        input: String,
    ): Either<Unit, AddEntryToShoppingListError> {
        if (input.isBlank()) {
            return AddEntryToShoppingListError.InvalidName.right()
        }
        val names = when {
            !extractor.supports(input) -> listOf(input)
            else -> runCatching {
                extractor.extractIngredients(Ksoup.parseGetRequest(input))
                    .map { it.description }
                    .takeIf { it.isNotEmpty() }
            }.getOrNull() ?: return AddEntryToShoppingListError.ExtractionError.right()
        }
        return runNewSuspendedTransactionCatchingAs(AddEntryToShoppingListError.Internal) txn@{
            val listId = ShoppingListEntity.findById(listId.toJavaUuid())?.id
                ?: return@txn AddEntryToShoppingListError.UnknownListId.right()
            addEntriesToShoppingListInTransaction(userId.toJavaUuid(), listId, names).left()
        }
    }

    private fun addEntriesToShoppingListInTransaction(
        userId: UUID,
        listId: EntityID<UUID>,
        names: List<String>,
    ) {
        if (names.isEmpty()) return

        val incrementedOrder = ShoppingListItemsTable
            .select(ShoppingListItemsTable.order.max())
            .where { ShoppingListItemsTable.listId eq listId }
            .singleOrNull()
            ?.get(ShoppingListItemsTable.order.max())
            ?.let { it as? Double }
            ?.let { it + 1.0 }
            ?: 0.0
        val createdAt = Clock.System.now()
        names.forEachIndexed { idx, name ->
            ShoppingListItemEntity.new {
                this.name = name
                this.listId = listId
                this.byUserId = userId
                this.createdAt = createdAt
                this.order = incrementedOrder + idx
            }
        }
    }

    override suspend fun markItemAsChecked(
        userId: Uuid,
        itemId: Uuid,
    ): Either<Unit, UpdateItemError> {
        return runNewSuspendedTransactionCatchingAs(UpdateItemError.Internal) txn@{
            val listItem = ShoppingListItemEntity.findById(itemId.toJavaUuid())
                ?: return@txn UpdateItemError.UnknownItemId.right()
            listItem.status = ShoppingListItemData.CheckedStatusData.Checked(
                changedAt = Clock.System.now(),
                byUserId = userId,
            )
            Unit.left()
        }
    }

    override suspend fun markItemAsUnchecked(
        itemId: Uuid,
    ): Either<Unit, UpdateItemError> {
        return runNewSuspendedTransactionCatchingAs(UpdateItemError.Internal) txn@{
            val item = ShoppingListItemEntity.findById(itemId.toJavaUuid())
                ?: return@txn UpdateItemError.UnknownItemId.right()
            item.status = Unchecked
            Unit.left()
        }
    }

    override suspend fun updateItemOrder(
        itemId: Uuid,
        order: Double,
    ): Either<Unit, UpdateItemError> {
        return runNewSuspendedTransactionCatchingAs(UpdateItemError.Internal) txn@{
            val item = ShoppingListItemEntity.findById(itemId.toJavaUuid())
                ?: return@txn UpdateItemError.UnknownItemId.right()
            item.order = order
            Unit.left()
        }
    }
}
