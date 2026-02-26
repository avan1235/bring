package `in`.procyk.bring.code

import ar.com.hjg.pngj.ImageLineInt
import ar.com.hjg.pngj.PngReader
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import `in`.procyk.bring.Code
import java.io.ByteArrayInputStream
import java.lang.System.arraycopy
import com.google.zxing.aztec.detector.Detector as AztecDetector
import com.google.zxing.datamatrix.detector.Detector as DataMatrixDetector
import com.google.zxing.pdf417.detector.Detector as PDF417Detector
import com.google.zxing.qrcode.detector.Detector as QRCodeDetector

data object CodeDetector {

    fun detectSingle(imageBytes: ByteArray): Code? {
        val luminanceData = decodePngToLuminance(imageBytes)
        val source = ByteArrayLuminanceSource(luminanceData.width, luminanceData.height, luminanceData.luminance)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val hints = buildHints()

        val result = try {
            MultiFormatReader().decode(bitmap, hints)
        } catch (_: NotFoundException) {
            try {
                val pureHints = hints.toMutableMap().apply { put(DecodeHintType.PURE_BARCODE, true) }
                MultiFormatReader().decode(bitmap, pureHints)
            } catch (_: NotFoundException) {
                null
            }
        } ?: return null

        val format = result.barcodeFormat
        val bitMatrix = extractBitMatrix(bitmap, result, hints) ?: return null
        val is1D = format in ONE_D_FORMATS

        return Code(
            text = result.text,
            format = format.toCode(),
            bits = Code.Bits(
                width = bitMatrix.width,
                height = if (is1D) 1 else bitMatrix.height
            ) { x, y -> bitMatrix.get(x, if (is1D) 0 else y) }
        )
    }

    private fun extractBitMatrix(bitmap: BinaryBitmap, result: Result, hints: Map<DecodeHintType, Any>): BitMatrix? {
        val format = result.barcodeFormat

        if (format in ONE_D_FORMATS) {
            return MultiFormatWriter().encode(result.text, format, 0, 1)
        }

        val blackMatrix = bitmap.blackMatrix
        return when (format) {
            BarcodeFormat.QR_CODE -> QRCodeDetector(blackMatrix).detect(hints).bits
            BarcodeFormat.DATA_MATRIX -> DataMatrixDetector(blackMatrix).detect().bits
            BarcodeFormat.MAXICODE -> DataMatrixDetector(blackMatrix).detect().bits
            BarcodeFormat.AZTEC -> AztecDetector(blackMatrix).detect().bits
            BarcodeFormat.PDF_417 -> {
                val result = PDF417Detector.detect(bitmap, hints, true)
                val points = result.points.getOrNull(0) ?: return null
                val bits = result.bits
                val minX = points.minOfOrNull { it.x }?.toInt() ?: 0
                val maxX = points.maxOfOrNull { it.x }?.toInt() ?: 0
                val minY = points.minOfOrNull { it.y }?.toInt() ?: 0
                val maxY = points.maxOfOrNull { it.y }?.toInt() ?: 0
                BitMatrix(maxX - minX + 1, maxY - minY + 1).apply {
                    for (x in minX..maxX) for (y in minY..maxY) if (bits[x, y]) set(x - minX, y - minY)
                }
            }

            else -> null
        }
    }

    private val ONE_D_FORMATS = setOf(
        BarcodeFormat.CODABAR,
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.CODE_128,
        BarcodeFormat.EAN_8,
        BarcodeFormat.EAN_13,
        BarcodeFormat.ITF,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.UPC_EAN_EXTENSION,
    )

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

            for (y in 0..<height) {
                val line = reader.readRow() as ImageLineInt
                val scanline = line.scanline

                for (x in 0..<width) {
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
            require(y in 0..<height) { "Row index out of range: $y" }
            val result = if (row == null || row.size < width) ByteArray(width) else row
            arraycopy(luminance, y * width, result, 0, width)
            return result
        }

        override fun getMatrix(): ByteArray = luminance.copyOf()

        override fun isCropSupported(): Boolean = true

        override fun crop(left: Int, top: Int, cropWidth: Int, cropHeight: Int): LuminanceSource {
            val cropped = ByteArray(cropWidth * cropHeight)
            for (row in 0..<cropHeight) arraycopy(
                luminance, (top + row) * width + left,
                cropped, row * cropWidth,
                cropWidth,
            )
            return ByteArrayLuminanceSource(cropWidth, cropHeight, cropped)
        }

        override fun isRotateSupported(): Boolean = true

        override fun rotateCounterClockwise(): LuminanceSource {
            val rotated = ByteArray(width * height)
            for (y in 0..<height) for (x in 0..<width)
                rotated[x * height + (height - 1 - y)] = luminance[y * width + x]
            return ByteArrayLuminanceSource(height, width, rotated)
        }
    }
}