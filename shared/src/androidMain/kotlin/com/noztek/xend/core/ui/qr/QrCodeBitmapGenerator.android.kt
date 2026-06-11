package com.noztek.xend.core.ui.qr

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

actual fun generateQrCodeImageBitmap(
    content: String,
    sizePx: Int,
): ImageBitmap? {
    if (content.isBlank() || sizePx <= 0) return null

    return runCatching {
        val matrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx,
            mapOf(EncodeHintType.MARGIN to 0),
        )
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)

        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(
                    x,
                    y,
                    if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE,
                )
            }
        }

        bitmap.asImageBitmap()
    }.getOrNull()
}
