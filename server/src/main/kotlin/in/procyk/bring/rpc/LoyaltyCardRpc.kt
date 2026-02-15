package `in`.procyk.bring.rpc

import `in`.procyk.bring.LoyaltyCardRpcPath
import `in`.procyk.bring.service.LoyaltyCardService
import `in`.procyk.bring.service.LoyaltyCardServiceImpl
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.rpc

internal fun Route.loyaltyCardRpc() {
    rpc(LoyaltyCardRpcPath) {
        registerService<LoyaltyCardService>(::LoyaltyCardServiceImpl)
    }
}
