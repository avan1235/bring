package `in`.procyk.bring.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import `in`.procyk.bring.Code
import `in`.procyk.bring.LoyaltyCardData
import `in`.procyk.bring.code.CodeDetector
import `in`.procyk.bring.code.CodeGenerator
import `in`.procyk.bring.code.deserialize
import `in`.procyk.bring.code.serialize
import `in`.procyk.bring.db.LoyaltyCardEntity
import `in`.procyk.bring.service.LoyaltyCardService.*
import kotlinx.datetime.toDeprecatedInstant
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class LoyaltyCardServiceImpl : LoyaltyCardService {

    override suspend fun createLoyaltyCard(
        label: String,
        image: ByteArray,
        byUserId: Uuid,
    ): Either<Uuid, AddLoyaltyCardError> {
        if (label.isBlank()) {
            return AddLoyaltyCardError.InvalidLabel.right()
        }
        val code = CodeDetector.detectSingle(image)
            ?: return AddLoyaltyCardError.NoCodeDetected.right()
        return txn(AddLoyaltyCardError.Internal) {
            LoyaltyCardEntity.new {
                this.label = label
                this.byUserId = byUserId.toJavaUuid()
                this.createdAt = Clock.System.now().toDeprecatedInstant()
                this.codeFormat = code.format
                this.codeText = code.text
                this.codeBits = code.bits.serialize()
            }.id.value.toKotlinUuid().left()
        }
    }

    override suspend fun getLoyaltyCard(
        cardId: Uuid,
    ): Either<LoyaltyCardData, GetLoyaltyCardError> {
        val cardUUID = cardId.toJavaUuid()
        return txn(GetLoyaltyCardError.Internal) {
            val card = LoyaltyCardEntity.findById(cardUUID)
                ?: return@txn GetLoyaltyCardError.UnknownCardId.right()
            card.toLoyaltyCardData().left()
        }
    }

    override suspend fun removeLoyaltyCard(
        cardId: Uuid,
        byUserId: Uuid,
    ): Either<Unit, RemoveLoyaltyCardError> {
        val cardUUID = cardId.toJavaUuid()
        return txn(RemoveLoyaltyCardError.Internal) {
            val card = LoyaltyCardEntity.findById(cardUUID)
            when {
                card == null -> RemoveLoyaltyCardError.UnknownCardId.right()
                card.byUserId != byUserId.toJavaUuid() -> Unit.left()
                else -> card.delete().left()
            }
        }
    }

    override suspend fun generateCode(
        code: Code,
        color: Int,
        width: Int,
        height: Int,
    ): Either<ByteArray, GenerateCodeError> =
        runCatching { CodeGenerator.generatePng(code, color, width = width, height = height) }.fold(
            onSuccess = { it.left() },
            onFailure = { GenerateCodeError.Internal.right() }
        )
}

private fun LoyaltyCardEntity.toLoyaltyCardData(): LoyaltyCardData =
    LoyaltyCardData(
        id = id.value.toKotlinUuid(),
        label = label,
        code = Code(
            text = codeText,
            bits = codeBits.deserialize(),
            format = codeFormat,
        )
    )
