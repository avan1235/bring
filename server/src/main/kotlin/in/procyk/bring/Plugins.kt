package `in`.procyk.bring

import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.cbor.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.serialization.cbor.cbor
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin

internal fun Application.plugins(
    dotenv: Dotenv,
    appModule: Module,
) {
    install(Krpc) {
        serialization {
            cbor(DefaultCbor)
        }
    }
    install(AutoHeadResponse)
    install(Resources)
    install(ContentNegotiation) {
        cbor(DefaultCbor)
    }
    installCors(dotenv)
    install(Koin) {
        modules(appModule)
    }
}

private fun Application.installCors(dotenv: Dotenv) {
    val corsPort = dotenv.env<String>("CORS_PORT")
    val corsHost = dotenv.env<String>("CORS_HOST")
    val corsScheme = dotenv.env<String>("CORS_SCHEME")
    install(CORS) {
        allowHost("$corsHost:$corsPort", schemes = corsScheme.split(','))
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.ContentLength)
        allowNonSimpleContentTypes = true
    }
}