package `in`.procyk.bring.rpc

import `in`.procyk.bring.ShoppingListRpcPath
import `in`.procyk.bring.service.ShoppingListService
import `in`.procyk.bring.service.ShoppingListServiceImpl
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.rpc

internal fun Route.shoppingListRpc() {
    rpc(ShoppingListRpcPath) {
        registerService<ShoppingListService> { ShoppingListServiceImpl(it) }
    }
}
