package `in`.procyk.bring.code

import ar.com.hjg.pngj.ImageInfo
import ar.com.hjg.pngj.ImageLineInt
import ar.com.hjg.pngj.PngWriter
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import `in`.procyk.bring.Code
import java.io.ByteArrayOutputStream

data object CodeGenerator {

    fun generatePng(
        code: Code,
        color: Int,
        width: Int,
        height: Int,
    ): ByteArray {
        val encodeHints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 0,
        )

        val bitMatrix = MultiFormatWriter().encode(
            code.rawText,
            code.format.toZxing(),
            width,
            height,
            encodeHints,
        )

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val fgR = (color shr 16) and 0xFF
        val fgG = (color shr 8) and 0xFF
        val fgB = color and 0xFF
        val fgA = (color shr 24) and 0xFF

        val imageInfo = ImageInfo(matrixWidth, matrixHeight, 8, true)

        return ByteArrayOutputStream().use { out ->
            val writer = PngWriter(out, imageInfo)
            for (y in 0 until matrixHeight) {
                val line = ImageLineInt(imageInfo)
                val scanline = line.scanline
                for (x in 0 until matrixWidth) {
                    val offset = x * 4
                    if (bitMatrix.get(x, y)) {
                        scanline[offset] = fgR
                        scanline[offset + 1] = fgG
                        scanline[offset + 2] = fgB
                        scanline[offset + 3] = fgA
                    } else {
                        scanline[offset] = 0
                        scanline[offset + 1] = 0
                        scanline[offset + 2] = 0
                        scanline[offset + 3] = 0
                    }
                }
                writer.writeRow(line)
            }
            writer.end()
            out.toByteArray()
        }
    }
}