package `in`.procyk.bring.service

import arrow.core.Either
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import `in`.procyk.bring.LoyaltyCardData
import `in`.procyk.bring.service.LoyaltyCardService.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

internal class LoyaltyCardTest : BringServerIntegrationTestCase() {

    @Test
    fun `create loyalty card with blank label returns InvalidLabel`() = runTest {
        val service = loyaltyCardService()

        val result = coroutineScope {
            val deferred = CompletableDeferred<Either<Uuid, AddLoyaltyCardError>>(parent = coroutineContext.job)
            service.durableCall {
                deferred.complete(createLoyaltyCard("   ", ByteArray(0), userId))
            }
            deferred.await()
        }

        val error = assertIs<Either.Right<AddLoyaltyCardError>>(result).value
        assertEquals(AddLoyaltyCardError.InvalidLabel, error)
    }

    @Test
    fun `create loyalty card with empty image returns NoCodeDetected`() = runTest {
        val service = loyaltyCardService()

        val result = coroutineScope {
            val deferred = CompletableDeferred<Either<Uuid, AddLoyaltyCardError>>(parent = coroutineContext.job)
            service.durableCall {
                deferred.complete(createLoyaltyCard("My card", ByteArray(0), userId))
            }
            deferred.await()
        }

        val error = assertIs<Either.Right<AddLoyaltyCardError>>(result).value
        assertEquals(AddLoyaltyCardError.NoCodeDetected, error)
    }

    @Test
    fun `get unknown loyalty card returns UnknownCardId`() = runTest {
        val service = loyaltyCardService()

        val result = coroutineScope {
            val deferred =
                CompletableDeferred<Either<LoyaltyCardData, GetLoyaltyCardError>>(parent = coroutineContext.job)
            service.durableCall {
                deferred.complete(getLoyaltyCard(Uuid.random()))
            }
            deferred.await()
        }

        val error = assertIs<Either.Right<GetLoyaltyCardError>>(result).value
        assertEquals(GetLoyaltyCardError.UnknownCardId, error)
    }

    @Test
    fun `remove unknown loyalty card returns UnknownCardId`() = runTest {
        val service = loyaltyCardService()

        val result = coroutineScope {
            val deferred = CompletableDeferred<Either<Unit, RemoveLoyaltyCardError>>(parent = coroutineContext.job)
            service.durableCall {
                deferred.complete(removeLoyaltyCard(Uuid.random(), userId))
            }
            deferred.await()
        }

        val error = assertIs<Either.Right<RemoveLoyaltyCardError>>(result).value
        assertEquals(RemoveLoyaltyCardError.UnknownCardId, error)
    }

    @Test
    fun `remove loyalty card by owner deletes it`() = runTest {
        val service = loyaltyCardService()

        val cardId = service.testCall {
            createLoyaltyCard("To be removed", qrPngBytes("owner-card"), userId)
        }

        service.testCall {
            removeLoyaltyCard(cardId, userId)
        }

        val result = coroutineScope {
            val deferred =
                CompletableDeferred<Either<LoyaltyCardData, GetLoyaltyCardError>>(parent = coroutineContext.job)
            service.durableCall {
                deferred.complete(getLoyaltyCard(cardId))
            }
            deferred.await()
        }

        val error = assertIs<Either.Right<GetLoyaltyCardError>>(result).value
        assertEquals(GetLoyaltyCardError.UnknownCardId, error)
    }

    @Test
    fun `remove loyalty card by non-owner does not delete it`() = runTest {
        val service = loyaltyCardService()
        val label = "Owner's card"

        val cardId = service.testCall {
            createLoyaltyCard(label, qrPngBytes("non-owner-card"), userId)
        }

        val otherUserId = Uuid.random()
        service.testCall {
            removeLoyaltyCard(cardId, otherUserId)
        }

        val data = service.testCall {
            getLoyaltyCard(cardId)
        }

        assertEquals(cardId, data.id)
        assertEquals(label, data.label)
    }

    private fun qrPngBytes(text: String): ByteArray {
        val size = 200
        val matrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val image = BufferedImage(matrix.width, matrix.height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                image.setRGB(x, y, if (matrix.get(x, y)) 0x000000 else 0xFFFFFF)
            }
        }
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        return out.toByteArray()
    }
}
