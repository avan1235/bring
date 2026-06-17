package `in`.procyk.savvry.rpc

import `in`.procyk.savvry.LoyaltyCardRpcPath
import `in`.procyk.savvry.service.LoyaltyCardService
import `in`.procyk.savvry.service.LoyaltyCardServiceImpl
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.rpc

internal fun Route.loyaltyCardRpc() {
    rpc(LoyaltyCardRpcPath) {
        registerService<LoyaltyCardService>(::LoyaltyCardServiceImpl)
    }
}
