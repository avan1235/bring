package `in`.procyk.bring.rpc

import `in`.procyk.bring.FavoriteElementRpcPath
import `in`.procyk.bring.service.FavoriteElementService
import `in`.procyk.bring.service.FavoriteElementServiceImpl
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.rpc

internal fun Route.favoriteElementRpc() {
    rpc(FavoriteElementRpcPath) {
        registerService<FavoriteElementService>(::FavoriteElementServiceImpl)
    }
}