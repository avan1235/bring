package `in`.procyk.bring.code

import ar.com.hjg.pngj.ImageLineInt
import ar.com.hjg.pngj.PngReader
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import `in`.procyk.bring.Code
import java.io.ByteArrayInputStream
import java.lang.System.arraycopy

data object CodeDetector {

    fun detectSingle(imageBytes: ByteArray): Code? {
        val luminanceData = decodePngToLuminance(imageBytes)
        val result = decodeSingle(luminanceData) ?: return null
        return Code(
            rawText = result.text,
            format = result.barcodeFormat.toCode(),
        )
    }

    private fun decodePngToLuminance(bytes: ByteArray): LuminanceData {
        require(bytes.isNotEmpty()) { "Image byte array must not be empty" }

        ByteArrayInputStream(bytes).use { stream ->
            val reader = PngReader(stream)
            val info = reader.imgInfo
            val width = info.cols
            val height = info.rows
            val channels = info.channels
            val hasAlpha = info.alpha

            val luminanceData = ByteArray(width * height)

            for (y in 0 until height) {
                val line = reader.readRow() as ImageLineInt
                val scanline = line.scanline

                for (x in 0 until width) {
                    val a: Int
                    val r: Int
                    val g: Int
                    val b: Int

                    when {
                        channels >= 3 -> {
                            val offset = x * channels
                            a = if (hasAlpha) scanline[offset + 3] else 255
                            r = scanline[offset]
                            g = scanline[offset + 1]
                            b = scanline[offset + 2]
                        }

                        channels == 2 -> { // grayscale + alpha
                            val offset = x * 2
                            a = scanline[offset + 1]
                            val gray = scanline[offset]
                            r = gray
                            g = gray
                            b = gray
                        }

                        else -> { // grayscale
                            a = 255
                            val gray = scanline[x]
                            r = gray
                            g = gray
                            b = gray
                        }
                    }

                    val rr = (r * a + 255 * (255 - a)) / 255
                    val gg = (g * a + 255 * (255 - a)) / 255
                    val bb = (b * a + 255 * (255 - a)) / 255

                    val luminance = ((306 * rr + 601 * gg + 117 * bb) shr 10).coerceIn(0, 255)
                    luminanceData[y * width + x] = luminance.toByte()
                }
            }
            reader.end()
            return LuminanceData(width, height, luminanceData)
        }
    }

    private fun decodeSingle(data: LuminanceData): Result? {
        val source = ByteArrayLuminanceSource(data.width, data.height, data.luminance)
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
        DecodeHintType.CHARACTER_SET to "UTF-8",
    )

    private class LuminanceData(
        val width: Int,
        val height: Int,
        val luminance: ByteArray,
    )

    private class ByteArrayLuminanceSource(
        width: Int,
        height: Int,
        private val luminance: ByteArray,
    ) : LuminanceSource(width, height) {

        override fun getRow(y: Int, row: ByteArray?): ByteArray {
            require(y in 0 until height) { "Row index out of range: $y" }
            val result = if (row == null || row.size < width) ByteArray(width) else row
            arraycopy(luminance, y * width, result, 0, width)
            return result
        }

        override fun getMatrix(): ByteArray = luminance.copyOf()

        override fun isCropSupported(): Boolean = true

        override fun crop(left: Int, top: Int, cropWidth: Int, cropHeight: Int): LuminanceSource {
            val cropped = ByteArray(cropWidth * cropHeight)
            for (row in 0 until cropHeight) arraycopy(
                luminance, (top + row) * width + left,
                cropped, row * cropWidth,
                cropWidth,
            )
            return ByteArrayLuminanceSource(cropWidth, cropHeight, cropped)
        }

        override fun isRotateSupported(): Boolean = true

        override fun rotateCounterClockwise(): LuminanceSource {
            val rotated = ByteArray(width * height)
            for (y in 0 until height) for (x in 0 until width)
                rotated[x * height + (height - 1 - y)] = luminance[y * width + x]
            return ByteArrayLuminanceSource(height, width, rotated)
        }
    }
}