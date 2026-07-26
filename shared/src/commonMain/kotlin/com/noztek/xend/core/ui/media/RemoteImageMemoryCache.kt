package com.noztek.xend.core.ui.media

import androidx.compose.ui.graphics.ImageBitmap

object RemoteImageMemoryCache {
    private val images = mutableMapOf<String, ImageBitmap>()

    fun get(key: String): ImageBitmap? = images[key]

    fun put(key: String, image: ImageBitmap) {
        images[key] = image
    }
}
