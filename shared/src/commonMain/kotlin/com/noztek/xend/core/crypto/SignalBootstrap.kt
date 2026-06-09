package com.noztek.xend.core.crypto

data class SignalBootstrap(
    val registrationId: Int,
    val identityKeyPublicBase64: String,
    val identityKeyPrivateBase64: String,
)
