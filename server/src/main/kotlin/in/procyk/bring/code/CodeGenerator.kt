package `in`.procyk.bring.code

import ar.com.hjg.pngj.ImageInfo
import ar.com.hjg.pngj.ImageLineInt
import ar.com.hjg.pngj.PngWriter
import `in`.procyk.bring.Code
import java.io.ByteArrayOutputStream

data object CodeGenerator {

    fun generatePng(
        code: Code,
        color: Int,
        width: Int,
        height: Int,
    ): ByteArray {
        val scale = maxOf(1, minOf(width / code.bits.width, height / code.bits.height))

        val leftPadding = maxOf(0, (width - (code.bits.width * scale)) / 2)
        val is1D = code.bits.height == 1
        val topPadding = if (is1D) 0 else maxOf(0, (height - (code.bits.height * scale)) / 2)

        val fgA = (color shr 24) and 0xFF
        val fgR = (color shr 16) and 0xFF
        val fgG = (color shr 8) and 0xFF
        val fgB = color and 0xFF

        val imageInfo = ImageInfo(width, height, 8, true)
        return ByteArrayOutputStream().use { out ->
            val writer = PngWriter(out, imageInfo)

            for (y in 0..<height) {
                val line = ImageLineInt(imageInfo)
                val scanline = line.scanline

                for (x in 0..<width) {
                    val offset = x * 4

                    val isBlack = when {
                        x >= leftPadding && y >= topPadding -> {
                            val moduleX = (x - leftPadding) / scale
                            val moduleY = if (is1D) 0 else (y - topPadding) / scale

                            if (moduleX < code.bits.width && moduleY < code.bits.height) code[moduleX, moduleY] else false
                        }

                        else -> false
                    }

                    if (isBlack) {
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