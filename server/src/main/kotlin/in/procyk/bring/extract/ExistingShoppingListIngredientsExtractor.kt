package `in`.procyk.bring.extract

import `in`.procyk.bring.ListIdRegex
import `in`.procyk.bring.db.ShoppingListEntity
import `in`.procyk.bring.db.ShoppingListItemEntity
import `in`.procyk.bring.db.ShoppingListItemsTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal data object ExistingShoppingListIngredientsExtractor : IngredientsExtractor {

    override suspend fun supports(input: String): Boolean =
        ListIdRegex.find(input) != null

    override suspend fun extractIngredients(input: String): List<Ingredient> {
        val match = ListIdRegex.find(input) ?: return emptyList()
        val id = Uuid.parseHexDash(match.value).toJavaUuid()
        return newSuspendedTransaction {
            ShoppingListItemEntity.find {
                ShoppingListItemsTable.listId eq id
            }.map { Ingredient(it.name) }
        }
    }

    override suspend fun extractTitle(input: String): String? {
        val match = ListIdRegex.find(input) ?: return null
        val id = Uuid.parseHexDash(match.value).toJavaUuid()
        return newSuspendedTransaction {
            ShoppingListEntity.findById(id)?.name
        }
    }
}