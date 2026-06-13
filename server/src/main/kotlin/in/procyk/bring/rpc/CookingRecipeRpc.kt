package `in`.procyk.bring.rpc

import `in`.procyk.bring.CookingRecipeRpcPath
import `in`.procyk.bring.service.CookingRecipeService
import `in`.procyk.bring.service.CookingRecipeServiceImpl
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.rpc

internal fun Route.cookingRecipeRpc() {
    rpc(CookingRecipeRpcPath) {
        registerService<CookingRecipeService>(::CookingRecipeServiceImpl)
    }
}
