package com.noztek.xend

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun currentDeviceName(): String =
    UIDevice.currentDevice.name.takeIf { it.isNotBlank() } ?: "iPhone"

actual fun currentPlatformId(): String = "ios"
