package `in`.procyk.bring.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import `in`.procyk.bring.FavoriteElement
import `in`.procyk.bring.UserFavoriteElementsData
import `in`.procyk.bring.db.FavoriteElementEntity
import `in`.procyk.bring.db.FavoriteElementsTable
import `in`.procyk.bring.service.FavoriteElementService.*
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class FavoriteElementServiceImpl(
    override val coroutineContext: CoroutineContext,
) : FavoriteElementService {

    private fun findFavoriteElements(byUserId: Uuid): Either<UserFavoriteElementsData, Nothing> {
        val elements = FavoriteElementEntity.find {
            FavoriteElementsTable.byUserId eq byUserId.toJavaUuid()
        }.map { entity ->
            FavoriteElement(
                id = entity.id.value.toKotlinUuid(),
                name = entity.name
            )
        }
        return UserFavoriteElementsData(elements).left()
    }

    override suspend fun getFavoriteElements(
        byUserId: Uuid
    ): Either<UserFavoriteElementsData, GetFavoriteElementsError> {
        return runNewSuspendedTransactionCatchingAs(GetFavoriteElementsError.Internal) {
            findFavoriteElements(byUserId)
        }
    }

    override suspend fun addFavoriteElement(
        name: String,
        byUserId: Uuid
    ): Either<UserFavoriteElementsData, AddFavoriteElementError> {
        if (name.isBlank()) {
            return AddFavoriteElementError.InvalidName.right()
        }
        return runNewSuspendedTransactionCatchingAs(AddFavoriteElementError.Internal) {
            FavoriteElementEntity.new {
                this.name = name
                this.byUserId = byUserId.toJavaUuid()
                this.createdAt = Clock.System.now()
            }
            findFavoriteElements(byUserId)
        }
    }

    override suspend fun removeFavoriteElement(
        elementId: Uuid
    ): Either<UserFavoriteElementsData, RemoveFavoriteElementError> {
        val elementUUID = elementId.toJavaUuid()
        return runNewSuspendedTransactionCatchingAs(RemoveFavoriteElementError.Internal) {
            val element = FavoriteElementEntity.findById(elementUUID)
            when {
                element == null -> RemoveFavoriteElementError.UnknownElementId.right()
                else -> {
                    val userId = element.byUserId.toKotlinUuid()
                    element.delete()
                    findFavoriteElements(userId)
                }
            }
        }
    }
}
