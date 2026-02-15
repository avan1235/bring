package `in`.procyk.bring.code

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import `in`.procyk.bring.Code
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

data object CodeDetector {

    fun detectSingle(imageBytes: ByteArray): Code? {
        val image = decodeBytes(imageBytes)
        val result = decodeSingle(image) ?: return null
        return Code(
            rawText = result.text,
            format = result.barcodeFormat.toCode(),
        )
    }

    private fun decodeBytes(bytes: ByteArray): BufferedImage {
        require(bytes.isNotEmpty()) { "Image byte array must not be empty" }
        return ByteArrayInputStream(bytes).use { stream ->
            ImageIO.read(stream)
                ?: throw IllegalArgumentException(
                    "Could not decode image from bytes. Supported formats: ${
                        ImageIO.getReaderFormatNames().joinToString()
                    }"
                )
        }
    }

    private fun decodeSingle(image: BufferedImage): Result? {
        val source = BufferedImageLuminanceSource(image)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val hints = buildHints()

        return try {
            MultiFormatReader().decode(bitmap, hints)
        } catch (_: NotFoundException) {
            try {
                val pureHints = hints.toMutableMap()
                pureHints[DecodeHintType.PURE_BARCODE] = true
                MultiFormatReader().decode(bitmap, pureHints)
            } catch (_: NotFoundException) {
                null
            }
        }
    }

    private fun buildHints(): Map<DecodeHintType, Any> = mapOf(
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.CHARACTER_SET to "UTF-8"
    )
}
