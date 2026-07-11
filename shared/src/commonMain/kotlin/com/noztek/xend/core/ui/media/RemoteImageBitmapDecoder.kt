package com.noztek.xend.core.ui.media

import androidx.compose.ui.graphics.ImageBitmap

expect fun decodeRemoteImageBitmap(bytes: ByteArray): ImageBitmap?
