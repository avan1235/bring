package `in`.procyk.bring.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import `in`.procyk.bring.Code
import `in`.procyk.bring.LoyaltyCardData
import `in`.procyk.bring.code.CodeDetector
import `in`.procyk.bring.code.CodeGenerator
import `in`.procyk.bring.db.LoyaltyCardEntity
import `in`.procyk.bring.service.LoyaltyCardService.*
import kotlinx.datetime.toDeprecatedInstant
import kotlinx.datetime.toStdlibInstant
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class LoyaltyCardServiceImpl : LoyaltyCardService {

    override suspend fun createLoyaltyCard(
        label: String,
        image: ByteArray,
        byUserId: Uuid,
    ): Either<LoyaltyCardData, AddLoyaltyCardError> {
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
                this.codeRawText = code.rawText
            }.let {
                LoyaltyCardData(
                    id = it.id.value.toKotlinUuid(),
                    label = it.label,
                    byUserId = it.byUserId.toKotlinUuid(),
                    createdAt = it.createdAt.toStdlibInstant(),
                    code = Code(
                        rawText = it.codeRawText,
                        format = it.codeFormat,
                    )
                ).left()
            }
        }
    }

    override suspend fun removeLoyaltyCard(
        cardId: Uuid,
    ): Either<Unit, RemoveLoyaltyCardError> {
        val cardUUID = cardId.toJavaUuid()
        return txn(RemoveLoyaltyCardError.Internal) {
            val element = LoyaltyCardEntity.findById(cardUUID)
            when {
                element == null -> RemoveLoyaltyCardError.UnknownCardId.right()
                else -> element.delete().left()
            }
        }
    }

    override suspend fun generateCode(
        code: Code,
    ): Either<ByteArray, GenerateCodeError> =
        runCatching { CodeGenerator.generatePng(code) }.fold(
            onSuccess = { it.left() },
            onFailure = { GenerateCodeError.Internal.right() }
        )
}