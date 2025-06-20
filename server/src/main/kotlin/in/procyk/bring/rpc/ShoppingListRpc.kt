package `in`.procyk.bring.rpc

import arrow.core.Either
import `in`.procyk.bring.ShoppingListData
import `in`.procyk.bring.ShoppingListRpcPath
import `in`.procyk.bring.service.ShoppingListService
import `in`.procyk.bring.service.ShoppingListService.GetShoppingListError
import `in`.procyk.bring.service.ShoppingListServiceImpl
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.krpc.ktor.server.rpc
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

internal fun Route.shoppingListRpc() {
    val sessions = ConcurrentHashMap<Uuid, Flow<Either<ShoppingListData, GetShoppingListError>?>>()
    rpc(ShoppingListRpcPath) {
        registerService<ShoppingListService> { ShoppingListServiceImpl(it, application, sessions) }
    }
}
