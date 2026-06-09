package com.noztek.xend.core.crypto

object SignalProtocol {
    const val PROTOCOL_DEVICE_SLOT = 1

    fun addressName(userId: String): String = "u" + userId.filter(Char::isLetterOrDigit)
}
