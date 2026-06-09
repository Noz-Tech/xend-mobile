package com.noztek.xend.feature.message.data.crypto

import com.noztek.xend.feature.device.data.local.dao.SignalSessionDao

data class EncryptedMessageEnvelope(
    val messageType: String,
    val ciphertextBase64: String,
)

class DuplicateSignalMessageException : IllegalStateException("Duplicate signal message")

interface SecureMessageCipher {
    fun encryptForSession(
        session: SignalSessionDao.SignalSessionLocal,
        plaintext: String,
    ): EncryptedMessageEnvelope

    fun decryptFromMessage(
        senderUserId: String,
        senderDeviceId: String,
        senderRegistrationId: Int,
        messageType: String,
        ciphertextBase64: String,
    ): String
}
