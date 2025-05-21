package `in`.procyk.bring

import `in`.procyk.bring.db.Database
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.koin.dsl.module

fun main() {
    val dotenv = dotenv {
        ignoreIfMissing = true
        directory = "../"
    }
    Database.init(dotenv)
    val appModule = module {
        single<Dotenv> { dotenv }
    }
    embeddedServer(
        factory = CIO,
        host = dotenv.env("HOST"),
        port = dotenv.env("PORT")
    ) {
        installPlugins(dotenv, appModule)
        installRoutes()
    }
        .start(wait = true)
}
