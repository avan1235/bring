package `in`.procyk.bring

import `in`.procyk.bring.rpc.favoriteElementRpc
import `in`.procyk.bring.rpc.shoppingListRpc
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal fun Application.installRoutes(): Routing = routing {
    get("/") {
        call.respond(HttpStatusCode.OK)
    }

    get("/version") {
        val version = "constant" // System.getenv("VERSION") ?: System.currentTimeMillis().toString()
        call.respond(version)
    }

    favoriteElementRpc()
    shoppingListRpc()
}

