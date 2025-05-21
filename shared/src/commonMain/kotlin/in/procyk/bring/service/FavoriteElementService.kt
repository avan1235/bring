package `in`.procyk.bring.service

import arrow.core.Either
import `in`.procyk.bring.UserFavoriteElementsData
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Rpc
interface FavoriteElementService : RemoteService {

    @Serializable
    enum class GetFavoriteElementsError { Internal }

    suspend fun getFavoriteElements(
        byUserId: Uuid,
    ): Either<UserFavoriteElementsData, GetFavoriteElementsError>

    @Serializable
    enum class AddFavoriteElementError { Internal, InvalidName }

    suspend fun addFavoriteElement(
        name: String,
        byUserId: Uuid,
    ): Either<UserFavoriteElementsData, AddFavoriteElementError>

    @Serializable
    enum class RemoveFavoriteElementError { Internal, UnknownElementId }

    suspend fun removeFavoriteElement(
        elementId: Uuid,
    ): Either<UserFavoriteElementsData, RemoveFavoriteElementError>
}
