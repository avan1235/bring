package `in`.procyk.savvry.rpc

import `in`.procyk.savvry.FavoriteElementRpcPath
import `in`.procyk.savvry.service.FavoriteElementService
import `in`.procyk.savvry.service.FavoriteElementServiceImpl
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.rpc

internal fun Route.favoriteElementRpc() {
    rpc(FavoriteElementRpcPath) {
        registerService<FavoriteElementService>(::FavoriteElementServiceImpl)
    }
}