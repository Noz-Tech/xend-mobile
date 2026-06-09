package com.noztek.xend

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentDeviceName(): String = Build.MODEL?.takeIf { it.isNotBlank() } ?: "Android Device"

actual fun currentPlatformId(): String = "android"
