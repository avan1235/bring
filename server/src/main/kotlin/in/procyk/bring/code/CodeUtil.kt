package `in`.procyk.bring.code

import `in`.procyk.bring.Code

internal fun Code.Format.toZxing(): com.google.zxing.BarcodeFormat = when (this) {
    Code.Format.QR_CODE -> com.google.zxing.BarcodeFormat.QR_CODE
    Code.Format.DATA_MATRIX -> com.google.zxing.BarcodeFormat.DATA_MATRIX
    Code.Format.AZTEC -> com.google.zxing.BarcodeFormat.AZTEC
    Code.Format.PDF_417 -> com.google.zxing.BarcodeFormat.PDF_417
    Code.Format.MAXICODE -> com.google.zxing.BarcodeFormat.MAXICODE
    Code.Format.CODE_128 -> com.google.zxing.BarcodeFormat.CODE_128
    Code.Format.CODE_39 -> com.google.zxing.BarcodeFormat.CODE_39
    Code.Format.CODE_93 -> com.google.zxing.BarcodeFormat.CODE_93
    Code.Format.EAN_13 -> com.google.zxing.BarcodeFormat.EAN_13
    Code.Format.EAN_8 -> com.google.zxing.BarcodeFormat.EAN_8
    Code.Format.UPC_A -> com.google.zxing.BarcodeFormat.UPC_A
    Code.Format.UPC_E -> com.google.zxing.BarcodeFormat.UPC_E
    Code.Format.ITF -> com.google.zxing.BarcodeFormat.ITF
    Code.Format.CODABAR -> com.google.zxing.BarcodeFormat.CODABAR
    Code.Format.RSS_14 -> com.google.zxing.BarcodeFormat.RSS_14
    Code.Format.RSS_EXPANDED -> com.google.zxing.BarcodeFormat.RSS_EXPANDED
    Code.Format.UPC_EAN_EXTENSION -> com.google.zxing.BarcodeFormat.UPC_EAN_EXTENSION
}

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