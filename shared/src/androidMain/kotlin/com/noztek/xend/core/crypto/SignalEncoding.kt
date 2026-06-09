package com.noztek.xend.core.crypto

import android.util.Base64
import java.security.MessageDigest

internal fun encodeBase64(bytes: ByteArray): String =
    Base64.encodeToString(bytes, Base64.NO_WRAP)

internal fun decodeBase64(value: String): ByteArray =
    Base64.decode(value, Base64.DEFAULT)

internal fun identityFingerprint(identityKeyPublicBase64: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(decodeBase64(identityKeyPublicBase64))
    return digest.joinToString(separator = "") { "%02x".format(it) }
}
