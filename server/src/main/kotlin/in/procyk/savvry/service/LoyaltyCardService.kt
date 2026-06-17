package `in`.procyk.savvry.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import `in`.procyk.savvry.Code
import `in`.procyk.savvry.LoyaltyCardData
import `in`.procyk.savvry.code.CodeDetector
import `in`.procyk.savvry.code.deserialize
import `in`.procyk.savvry.code.serialize
import `in`.procyk.savvry.db.LoyaltyCardEntity
import `in`.procyk.savvry.service.LoyaltyCardService.*
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
