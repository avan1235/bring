package `in`.procyk.bring.code

import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import `in`.procyk.bring.Code
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

data object CodeGenerator {

    fun generatePng(
        code: Code,
        width: Int = 300,
        height: Int = 300,
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
            encodeHints
        )

        val image = BufferedImage(bitMatrix.width, bitMatrix.height, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until bitMatrix.width) for (y in 0 until bitMatrix.height)
            image.setRGB(x, y, if (bitMatrix[x, y]) 0x000000 else 0xFFFFFF)

        return ByteArrayOutputStream().use {
            ImageIO.write(image, "PNG", it)
            it.toByteArray()
        }
    }
}
