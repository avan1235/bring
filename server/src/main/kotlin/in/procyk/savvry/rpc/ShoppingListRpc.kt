package `in`.procyk.savvry.rpc

import arrow.core.Either
import `in`.procyk.savvry.ShoppingListData
import `in`.procyk.savvry.ShoppingListRpcPath
import `in`.procyk.savvry.service.ShoppingListService
import `in`.procyk.savvry.service.ShoppingListService.GetShoppingListError
import `in`.procyk.savvry.service.ShoppingListServiceImpl
import `in`.procyk.savvry.service.ShoppingListServiceImpl.UpdateRequest
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.rpc.krpc.ktor.server.rpc
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

internal fun Route.shoppingListRpc() {
    val listUpdateRequests = ConcurrentHashMap<Uuid, MutableStateFlow<UpdateRequest>>()
    val listUpdates = ConcurrentHashMap<Uuid, SharedFlow<Either<ShoppingListData, GetShoppingListError>?>>()
    rpc(ShoppingListRpcPath) {
        registerService<ShoppingListService> { ShoppingListServiceImpl(application, listUpdateRequests, listUpdates) }
    }
}
