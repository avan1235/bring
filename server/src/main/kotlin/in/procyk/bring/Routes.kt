package `in`.procyk.bring

import `in`.procyk.bring.rpc.favoriteElementRpc
import `in`.procyk.bring.rpc.loyaltyCardRpc
import `in`.procyk.bring.rpc.shoppingListRpc
import `in`.procyk.bring.server.BuildConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal fun Application.routes() = routing {
    get("/") {
        call.respondRedirect(
            url = "https://bring.procyk.in",
            permanent = true,
        )
    }
    get("/version") {
        call.respond(BuildConfig.VERSION)
    }
    get("/health") {
        call.respond(HttpStatusCode.OK)
    }
    loyaltyCardRpc()
    favoriteElementRpc()
    shoppingListRpc()
}

