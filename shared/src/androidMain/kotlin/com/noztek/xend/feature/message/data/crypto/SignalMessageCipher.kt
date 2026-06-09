package com.noztek.xend.feature.message.data.crypto

import com.noztek.xend.core.crypto.decodeBase64
import com.noztek.xend.core.crypto.encodeBase64
import com.noztek.xend.feature.device.data.local.dao.DeviceDao
import com.noztek.xend.feature.device.data.local.dao.KyberPrekeyDao
import com.noztek.xend.feature.device.data.local.dao.OneTimePrekeyDao
import com.noztek.xend.feature.device.data.local.dao.SignalSessionDao
import com.noztek.xend.feature.device.data.local.dao.SignedPrekeyDao
import org.signal.libsignal.protocol.DuplicateMessageException
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.SessionCipher
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.ecc.ECKeyPair
import org.signal.libsignal.protocol.ecc.ECPrivateKey
import org.signal.libsignal.protocol.ecc.ECPublicKey
import org.signal.libsignal.protocol.message.CiphertextMessage
import org.signal.libsignal.protocol.message.PreKeySignalMessage
import org.signal.libsignal.protocol.message.SignalMessage
import org.signal.libsignal.protocol.state.KyberPreKeyRecord
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SessionRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore

class SignalMessageCipher(
    private val deviceDao: DeviceDao,
    private val signedPrekeyDao: SignedPrekeyDao,
    private val kyberPrekeyDao: KyberPrekeyDao,
    private val oneTimePrekeyDao: OneTimePrekeyDao,
    private val signalSessionDao: SignalSessionDao,
) : SecureMessageCipher {
    override
    fun encryptForSession(session: SignalSessionDao.SignalSessionLocal, plaintext: String): EncryptedMessageEnvelope {
        val store = buildLocalStore()
        val remoteAddress = SignalProtocolAddress(session.protocolAddressName, session.protocolDeviceSlot)
        val recordBytes = requireNotNull(session.sessionRecordBase64) { "Missing session record" }
        store.storeSession(remoteAddress, SessionRecord(decodeBase64(recordBytes)))

        val cipher = SessionCipher(store, remoteAddress)
        val encrypted = cipher.encrypt(plaintext.toByteArray(Charsets.UTF_8))
        val updatedRecord = store.loadSession(remoteAddress)

        signalSessionDao.upsertBootstrapReadySession(
            userId = session.userId,
            deviceId = session.deviceId,
            remoteIdentityFingerprint = session.remoteIdentityFingerprint,
            registrationId = session.registrationId,
            sessionState = if (updatedRecord.hasSenderChain()) "active" else session.sessionState,
            signedPrekeyId = session.signedPrekeyId,
            signedPrekeyPublic = session.signedPrekeyPublic,
            signedPrekeySignature = session.signedPrekeySignature,
            oneTimePrekeyId = session.oneTimePrekeyId,
            oneTimePrekeyPublic = session.oneTimePrekeyPublic,
            sessionRecordBase64 = encodeBase64(updatedRecord.serialize()),
        )

        val type = when (encrypted.type) {
            CiphertextMessage.PREKEY_TYPE -> "signal_prekey"
            else -> "signal_whisper"
        }

        return EncryptedMessageEnvelope(
            messageType = type,
            ciphertextBase64 = encodeBase64(encrypted.serialize()),
        )
    }

    override
    fun decryptFromMessage(
        senderUserId: String,
        senderDeviceId: String,
        senderRegistrationId: Int,
        messageType: String,
        ciphertextBase64: String,
    ): String {
        val store = buildLocalStore()
        val remoteSession = requireNotNull(signalSessionDao.getDeterministicSession(senderUserId, senderDeviceId)) {
            "Missing deterministic session for sender"
        }
        val remoteAddress = SignalProtocolAddress(
            remoteSession.protocolAddressName,
            remoteSession.protocolDeviceSlot,
        )
        if (remoteSession.sessionRecordBase64 != null) {
            store.storeSession(
                remoteAddress,
                SessionRecord(decodeBase64(remoteSession.sessionRecordBase64)),
            )
        }

        val cipher = SessionCipher(store, remoteAddress)
        val ciphertext = decodeBase64(ciphertextBase64)
        val plaintext = try {
            when (messageType) {
                "signal_prekey" -> {
                    val message = PreKeySignalMessage(ciphertext)
                    val result = cipher.decrypt(message)
                    message.preKeyId.ifPresent { keyId ->
                        val current = requireNotNull(deviceDao.getCurrentDeviceId()) { "Missing current device id" }
                        oneTimePrekeyDao.markUsed(current, keyId)
                    }
                    result
                }

                else -> cipher.decrypt(SignalMessage(ciphertext))
            }
        } catch (_: DuplicateMessageException) {
            throw DuplicateSignalMessageException()
        }

        val updatedRecord = store.loadSession(remoteAddress)
        val state = if (updatedRecord.hasSenderChain()) "active" else "bootstrap_ready"
        signalSessionDao.upsertBootstrapReadySession(
            userId = senderUserId,
            deviceId = senderDeviceId,
            remoteIdentityFingerprint = remoteSession.remoteIdentityFingerprint,
            registrationId = senderRegistrationId,
            sessionState = state,
            signedPrekeyId = remoteSession.signedPrekeyId,
            signedPrekeyPublic = remoteSession.signedPrekeyPublic,
            signedPrekeySignature = remoteSession.signedPrekeySignature,
            oneTimePrekeyId = remoteSession.oneTimePrekeyId,
            oneTimePrekeyPublic = remoteSession.oneTimePrekeyPublic,
            sessionRecordBase64 = encodeBase64(updatedRecord.serialize()),
        )

        return plaintext.toString(Charsets.UTF_8)
    }

    private fun buildLocalStore(): InMemorySignalProtocolStore {
        val bootstrap = requireNotNull(deviceDao.getCurrentSignalBootstrap()) { "Missing local device identity" }
        val identityKeyPair = IdentityKeyPair(
            IdentityKey(decodeBase64(bootstrap.identityKeyPublicBase64)),
            ECPrivateKey(decodeBase64(bootstrap.identityKeyPrivateBase64)),
        )
        val store = InMemorySignalProtocolStore(identityKeyPair, bootstrap.registrationId)
        val currentDeviceId = requireNotNull(deviceDao.getCurrentDeviceId()) { "Missing current device id" }

        signedPrekeyDao.getAllForDevice(currentDeviceId).forEach { prekey ->
            store.storeSignedPreKey(
                prekey.keyId,
                SignedPreKeyRecord(
                    prekey.keyId,
                    prekey.uploadedAtEpochSeconds ?: (System.currentTimeMillis() / 1000),
                    ECKeyPair(
                        ECPublicKey(decodeBase64(prekey.publicKeyBase64)),
                        ECPrivateKey(decodeBase64(prekey.privateKeyBase64)),
                    ),
                    decodeBase64(prekey.signatureBase64),
                ),
            )
        }

        kyberPrekeyDao.getLatestForDevice(currentDeviceId)?.let { prekey ->
            store.storeKyberPreKey(
                prekey.keyId,
                KyberPreKeyRecord(
                    decodeBase64(requireNotNull(prekey.recordBase64) { "Missing kyber prekey record" }),
                ),
            )
        }

        oneTimePrekeyDao.getAllForDevice(currentDeviceId).forEach { prekey ->
            store.storePreKey(
                prekey.keyId,
                PreKeyRecord(
                    prekey.keyId,
                    ECKeyPair(
                        ECPublicKey(decodeBase64(prekey.publicKeyBase64)),
                        ECPrivateKey(decodeBase64(prekey.privateKeyBase64)),
                    ),
                ),
            )
        }

        return store
    }
}
