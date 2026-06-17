package `in`.procyk.savvry

import `in`.procyk.savvry.rpc.cookingRecipeRpc
import `in`.procyk.savvry.rpc.favoriteElementRpc
import `in`.procyk.savvry.rpc.loyaltyCardRpc
import `in`.procyk.savvry.rpc.shoppingListRpc
import `in`.procyk.savvry.server.BuildConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal fun Application.routes() = routing {
    get("/") {
        call.respondRedirect(
            url = "https://savvry.procyk.in",
            permanent = true,
        )
    }
    get("/version") {
        call.respond(BuildConfig.VERSION)
    }
    get("/health") {
        call.respond(HttpStatusCode.OK)
    }
    cookingRecipeRpc()
    loyaltyCardRpc()
    favoriteElementRpc()
    shoppingListRpc()
}

