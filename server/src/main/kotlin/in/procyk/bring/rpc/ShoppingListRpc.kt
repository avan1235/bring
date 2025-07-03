package `in`.procyk.bring.rpc

import arrow.core.Either
import `in`.procyk.bring.ShoppingListData
import `in`.procyk.bring.ShoppingListRpcPath
import `in`.procyk.bring.service.ShoppingListService
import `in`.procyk.bring.service.ShoppingListService.GetShoppingListError
import `in`.procyk.bring.service.ShoppingListServiceImpl
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.rpc.krpc.ktor.server.rpc
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

internal fun Route.shoppingListRpc() {
    val listUpdates = ConcurrentHashMap<Uuid, SharedFlow<Either<ShoppingListData, GetShoppingListError>?>>()
    rpc(ShoppingListRpcPath) {
        registerService<ShoppingListService> { ShoppingListServiceImpl(application, listUpdates) }
    }
}
