package `in`.procyk.bring.service

import arrow.core.Either
import `in`.procyk.bring.ShoppingListData
import `in`.procyk.bring.UserShoppingListSuggestionsData
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Rpc
interface ShoppingListService : RemoteService {

    @Serializable
    enum class CreateNewShoppingListError { Internal, ExtractionError, InvalidName }

    suspend fun createNewShoppingList(
        userId: Uuid,
        input: String,
    ): Either<Uuid, CreateNewShoppingListError>

    @Serializable
    enum class RemoveShoppingListItemError { Internal, UnknownItemId }

    suspend fun removeShoppingListItem(
        itemId: Uuid,
    ): Either<Unit, RemoveShoppingListItemError>

    @Serializable
    enum class GetShoppingListError { Internal, UnknownListId }

    fun getShoppingList(
        listId: Uuid,
    ): Flow<Either<ShoppingListData, GetShoppingListError>>

    @Serializable
    enum class GetUserShoppingListSuggestionsError { Internal }

    suspend fun getUserShoppingListSuggestions(
        userId: Uuid,
        limit: Int = 10,
    ): Either<UserShoppingListSuggestionsData, GetUserShoppingListSuggestionsError>

    @Serializable
    enum class ReorderListError { Internal }

    suspend fun reorderListItems(
        listId: Uuid,
    ): Either<Unit, ReorderListError>

    @Serializable
    enum class AddEntryToShoppingListError { Internal, InvalidName, ExtractionError, UnknownListId }

    suspend fun addEntryToShoppingList(
        userId: Uuid,
        listId: Uuid,
        input: String,
    ): Either<Unit, AddEntryToShoppingListError>

    @Serializable
    enum class UpdateItemError { Internal, UnknownItemId }

    suspend fun markItemAsChecked(
        userId: Uuid,
        itemId: Uuid,
    ): Either<Unit, UpdateItemError>

    suspend fun markItemAsUnchecked(
        itemId: Uuid,
    ): Either<Unit, UpdateItemError>

    suspend fun updateItemOrder(
        itemId: Uuid,
        order: Double,
    ): Either<Unit, UpdateItemError>
}
