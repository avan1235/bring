package `in`.procyk.savvry.rpc

import `in`.procyk.savvry.CookingRecipeRpcPath
import `in`.procyk.savvry.service.CookingRecipeService
import `in`.procyk.savvry.service.CookingRecipeServiceImpl
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.rpc

internal fun Route.cookingRecipeRpc() {
    rpc(CookingRecipeRpcPath) {
        registerService<CookingRecipeService>(::CookingRecipeServiceImpl)
    }
}
