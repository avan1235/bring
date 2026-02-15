package `in`.procyk.bring.service

import arrow.core.Either
import `in`.procyk.bring.Code
import `in`.procyk.bring.LoyaltyCardData
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Rpc
interface LoyaltyCardService {

    @Serializable
    enum class AddLoyaltyCardError { NoCodeDetected, InvalidLabel, Internal }

    suspend fun createLoyaltyCard(
        label: String,
        image: ByteArray,
        byUserId: Uuid,
    ): Either<Uuid, AddLoyaltyCardError>

    @Serializable
    enum class GetLoyaltyCardError { UnknownCardId, Internal }

    suspend fun getLoyaltyCard(
        cardId: Uuid,
    ): Either<LoyaltyCardData, GetLoyaltyCardError>

    @Serializable
    enum class RemoveLoyaltyCardError { UnknownCardId, Internal }

    suspend fun removeLoyaltyCard(
        cardId: Uuid,
    ): Either<Unit, RemoveLoyaltyCardError>

    @Serializable
    enum class GenerateCodeError { Internal }

    suspend fun generateCode(
        code: Code,
        color: Int,
        width: Int,
        height: Int,
    ): Either<ByteArray, GenerateCodeError>
}