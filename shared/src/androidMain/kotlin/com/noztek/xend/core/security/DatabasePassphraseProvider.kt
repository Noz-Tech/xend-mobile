package com.noztek.xend.core.security

import android.content.Context
import androidx.core.content.edit
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

class DatabasePassphraseProvider(
    private val context: Context,
) {
    fun getPassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encoded = prefs.getString(KEY_DB_PASSPHRASE_ENCRYPTED, null)

        val passphrase = if (encoded != null) {
            runCatching { decrypt(encoded) }
                .getOrElse { error ->
                    if (isRecoverableDecryptFailure(error)) {
                        val regenerated = generatePassphrase()
                        prefs.edit {
                            remove(KEY_DB_PASSPHRASE_ENCRYPTED)
                            putString(KEY_DB_PASSPHRASE_ENCRYPTED, encrypt(regenerated))
                        }
                        regenerated
                    } else {
                        throw error
                    }
                }
        } else {
            generatePassphrase().also {
                prefs.edit { putString(KEY_DB_PASSPHRASE_ENCRYPTED, encrypt(it)) }
            }
        }

        return passphrase.toByteArray(Charsets.UTF_8)
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val payload = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, payload, 0, iv.size)
        System.arraycopy(cipherText, 0, payload, iv.size, cipherText.size)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload)
    }

    private fun decrypt(encodedPayload: String): String {
        val payload = Base64.getUrlDecoder().decode(encodedPayload)
        require(payload.size > IV_SIZE_BYTES) { "invalid encrypted payload" }

        val iv = payload.copyOfRange(0, IV_SIZE_BYTES)
        val cipherText = payload.copyOfRange(IV_SIZE_BYTES, payload.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv),
        )

        val plainBytes = cipher.doFinal(cipherText)
        return plainBytes.toString(Charsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null)
        if (existing is SecretKey) return existing

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun generatePassphrase(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun isRecoverableDecryptFailure(error: Throwable): Boolean {
        return error is AEADBadTagException ||
            error is IllegalArgumentException ||
            error.cause?.let(::isRecoverableDecryptFailure) == true
    }

    private companion object {
        const val PREFS_NAME = "xend_secure_storage"
        const val KEY_DB_PASSPHRASE_ENCRYPTED = "db_passphrase_encrypted"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "xend_db_passphrase_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE_BYTES = 12
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
