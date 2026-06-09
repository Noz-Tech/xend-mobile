package com.noztek.xend

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun currentDeviceName(): String

expect fun currentPlatformId(): String
