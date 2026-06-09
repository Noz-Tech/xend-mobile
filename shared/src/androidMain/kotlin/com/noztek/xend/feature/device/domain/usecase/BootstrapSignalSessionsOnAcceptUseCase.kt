package com.noztek.xend.feature.device.domain.usecase

import android.util.Log
import com.noztek.xend.core.crypto.SignalProtocol
import com.noztek.xend.core.crypto.decodeBase64
import com.noztek.xend.core.crypto.encodeBase64
import com.noztek.xend.core.crypto.identityFingerprint
import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.feature.device.data.local.dao.DeviceDao
import com.noztek.xend.feature.device.data.local.dao.RemoteIdentityDecision
import com.noztek.xend.feature.device.data.local.dao.SignalSessionDao
import com.noztek.xend.feature.device.data.remote.DeviceApi
import com.noztek.xend.feature.space.data.remote.SpaceApi
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.SessionBuilder
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.ecc.ECPrivateKey
import org.signal.libsignal.protocol.ecc.ECPublicKey
import org.signal.libsignal.protocol.kem.KEMPublicKey
import org.signal.libsignal.protocol.state.PreKeyBundle
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore

class BootstrapSignalSessionsOnAcceptUseCase(
    private val authSessionDao: AuthSessionDao,
    private val deviceDao: DeviceDao,
    private val spaceApi: SpaceApi,
    private val deviceApi: DeviceApi,
    private val signalSessionDao: SignalSessionDao,
) : SignalSessionBootstrapper {
    override suspend fun bootstrap(targetUserIds: Collection<String>?) {
        val session = authSessionDao.getCurrentSession() ?: return
        val local = deviceDao.getCurrentSignalBootstrap() ?: return
        val requestedUsers = targetUserIds?.toSet()

        val localIdentityPair = IdentityKeyPair(
            IdentityKey(decodeBase64(local.identityKeyPublicBase64)),
            ECPrivateKey(decodeBase64(local.identityKeyPrivateBase64)),
        )
        val protocolStore = InMemorySignalProtocolStore(localIdentityPair, local.registrationId)
        val seenUsers = mutableSetOf<String>()
        val spaces = spaceApi.getSpaces(session.accessToken)

        for (space in spaces) {
            val members = runCatching {
                spaceApi.getSpaceMembers(session.accessToken, space.relationshipSpaceId)
            }.getOrDefault(emptyList())

            for (member in members) {
                if (member.userId == session.userId || !seenUsers.add(member.userId)) continue
                if (requestedUsers != null && member.userId !in requestedUsers) continue

                runCatching {
                    val bundle = deviceApi.getRecipientPrekeys(session.accessToken, member.userId)
                    bundle.devices.forEach { device ->
                        val remoteFingerprint = identityFingerprint(device.identityKeyPublic)
                        runCatching {
                            val trustDecision = signalSessionDao.recordRemoteIdentity(
                                userId = member.userId,
                                deviceId = device.deviceId,
                                identityKeyPublic = device.identityKeyPublic,
                                identityFingerprint = remoteFingerprint,
                            )
                            if (trustDecision == RemoteIdentityDecision.CHANGED) {
                                error("remote identity changed for user=${member.userId} device=${device.deviceId}")
                            }

                            val existing = signalSessionDao.getDeterministicSession(member.userId, device.deviceId)
                            if (existing != null && !existing.resetRequired && existing.sessionRecordBase64 != null) {
                                return@runCatching
                            }

                            val remoteAddress = SignalProtocolAddress(
                                SignalProtocol.addressName(member.userId),
                                SignalProtocol.PROTOCOL_DEVICE_SLOT,
                            )
                            val remoteIdentityKey = IdentityKey(decodeBase64(device.identityKeyPublic))
                            val signedPrekeyPublic = ECPublicKey(decodeBase64(device.signedPrekey.publicKey))
                            val oneTime = device.oneTimePrekey
                            val oneTimePrekeyPublic = oneTime?.publicKey?.let { ECPublicKey(decodeBase64(it)) }
                            val kyberPrekeyPublic = KEMPublicKey(decodeBase64(device.kyberPrekey.publicKey))
                            val kyberSignature = decodeBase64(device.kyberPrekey.signature)

                            val preKeyBundle = PreKeyBundle(
                                device.registrationId,
                                SignalProtocol.PROTOCOL_DEVICE_SLOT,
                                oneTime?.keyId ?: PreKeyBundle.NULL_PRE_KEY_ID,
                                oneTimePrekeyPublic,
                                device.signedPrekey.keyId,
                                signedPrekeyPublic,
                                decodeBase64(device.signedPrekey.signature),
                                remoteIdentityKey,
                                device.kyberPrekey.keyId,
                                kyberPrekeyPublic,
                                kyberSignature,
                            )

                            SessionBuilder(protocolStore, remoteAddress).process(preKeyBundle)
                            val sessionRecord = protocolStore.loadSession(remoteAddress)
                            val recordSerialized = encodeBase64(sessionRecord.serialize())
                            val state = if (sessionRecord.hasSenderChain()) "active" else "bootstrap_ready"

                            signalSessionDao.upsertBootstrapReadySession(
                                userId = member.userId,
                                deviceId = device.deviceId,
                                remoteIdentityFingerprint = remoteFingerprint,
                                registrationId = device.registrationId,
                                sessionState = state,
                                signedPrekeyId = device.signedPrekey.keyId,
                                signedPrekeyPublic = device.signedPrekey.publicKey,
                                signedPrekeySignature = device.signedPrekey.signature,
                                oneTimePrekeyId = oneTime?.keyId,
                                oneTimePrekeyPublic = oneTime?.publicKey,
                                sessionRecordBase64 = recordSerialized,
                            )
                        }.onFailure {
                            signalSessionDao.upsertBootstrapReadySession(
                                userId = member.userId,
                                deviceId = device.deviceId,
                                remoteIdentityFingerprint = remoteFingerprint,
                                registrationId = device.registrationId,
                                sessionState = "failed",
                                signedPrekeyId = device.signedPrekey.keyId,
                                signedPrekeyPublic = device.signedPrekey.publicKey,
                                signedPrekeySignature = device.signedPrekey.signature,
                                oneTimePrekeyId = device.oneTimePrekey?.keyId,
                                oneTimePrekeyPublic = device.oneTimePrekey?.publicKey,
                                sessionRecordBase64 = null,
                            )
                            throw it
                        }
                    }
                }.onFailure {
                    Log.w(TAG, "prekey bootstrap failed for user=${member.userId}: ${it.message}")
                }
            }
        }
    }

    suspend operator fun invoke(targetUserIds: Collection<String>? = null) {
        bootstrap(targetUserIds)
    }

    private companion object {
        const val TAG = "SignalBootstrap"
    }
}
