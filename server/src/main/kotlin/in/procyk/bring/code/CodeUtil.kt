package `in`.procyk.bring.code

import `in`.procyk.bring.Code
import java.nio.ByteBuffer


internal fun com.google.zxing.BarcodeFormat.toCode(): Code.Format = when (this) {
    com.google.zxing.BarcodeFormat.AZTEC -> Code.Format.AZTEC
    com.google.zxing.BarcodeFormat.CODABAR -> Code.Format.CODABAR
    com.google.zxing.BarcodeFormat.CODE_39 -> Code.Format.CODE_39
    com.google.zxing.BarcodeFormat.CODE_93 -> Code.Format.CODE_93
    com.google.zxing.BarcodeFormat.CODE_128 -> Code.Format.CODE_128
    com.google.zxing.BarcodeFormat.DATA_MATRIX -> Code.Format.DATA_MATRIX
    com.google.zxing.BarcodeFormat.EAN_8 -> Code.Format.EAN_8
    com.google.zxing.BarcodeFormat.EAN_13 -> Code.Format.EAN_13
    com.google.zxing.BarcodeFormat.ITF -> Code.Format.ITF
    com.google.zxing.BarcodeFormat.MAXICODE -> Code.Format.MAXICODE
    com.google.zxing.BarcodeFormat.PDF_417 -> Code.Format.PDF_417
    com.google.zxing.BarcodeFormat.QR_CODE -> Code.Format.QR_CODE
    com.google.zxing.BarcodeFormat.RSS_14 -> Code.Format.RSS_14
    com.google.zxing.BarcodeFormat.RSS_EXPANDED -> Code.Format.RSS_EXPANDED
    com.google.zxing.BarcodeFormat.UPC_A -> Code.Format.UPC_A
    com.google.zxing.BarcodeFormat.UPC_E -> Code.Format.UPC_E
    com.google.zxing.BarcodeFormat.UPC_EAN_EXTENSION -> Code.Format.UPC_EAN_EXTENSION
}

internal fun Code.Bits.serialize(): ByteArray {
    val totalBits = width * height
    val requiredBytes = (totalBits + 7) / 8

    val buffer = ByteBuffer.allocate(8 + requiredBytes)
    buffer.putInt(width)
    buffer.putInt(height)

    val packedBits = ByteArray(requiredBytes)
    for (y in 0..<height) for (x in 0..<width) {
        if (this[x, y]) {
            val bitIndex = y * width + x
            val bytePos = bitIndex / 8
            val bitPos = 7 - (bitIndex % 8)
            packedBits[bytePos] = (packedBits[bytePos].toInt() or (1 shl bitPos)).toByte()
        }
    }
    buffer.put(packedBits)
    return buffer.array()
}

internal fun ByteArray.deserialize(): Code.Bits = ByteBuffer.wrap(this).let { buffer ->
    val width = buffer.getInt()
    val height = buffer.getInt()

    return Code.Bits(
        width = width,
        height = height
    ) { x, y ->
        val bitIndex = y * width + x
        val bytePos = 8 + (bitIndex / 8)
        val bitPos = 7 - (bitIndex % 8)

        (this[bytePos].toInt() and (1 shl bitPos)) != 0
    }
}
