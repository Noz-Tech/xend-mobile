package com.noztek.xend.core.ui.qr

import androidx.compose.ui.graphics.ImageBitmap

expect fun generateQrCodeImageBitmap(
    content: String,
    sizePx: Int,
): ImageBitmap?
