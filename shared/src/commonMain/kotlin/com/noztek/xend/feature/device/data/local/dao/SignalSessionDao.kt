package com.noztek.xend.feature.device.data.local.dao

import com.noztek.xend.core.crypto.SignalProtocol
import com.noztek.xend.core.time.currentEpochSeconds
import org.noztek.Database

enum class RemoteIdentityDecision {
    TRUSTED,
    MATCHED,
    CHANGED,
}

class SignalSessionDao(
    private val db: Database,
) {
    companion object {
        const val STALE_REASON_REMOTE_IDENTITY_CHANGED = "remote_identity_changed"
        const val STALE_REASON_SEND_FAILED = "send_failed"
        const val STALE_REASON_DECRYPT_FAILED = "decrypt_failed"
        const val STALE_REASON_RECIPIENT_REINSTALLED = "recipient_reinstalled"
    }

    data class RemoteIdentityLocal(
        val userId: String,
        val deviceId: String,
        val identityKeyPublic: String,
        val identityFingerprint: String,
        val trustLevel: String,
    )

    data class SignalSessionLocal(
        val userId: String,
        val deviceId: String,
        val protocolAddressName: String,
        val protocolDeviceSlot: Int,
        val registrationId: Int,
        val remoteIdentityFingerprint: String,
        val sessionState: String,
        val resetRequired: Boolean,
        val staleReason: String?,
        val signedPrekeyId: Int,
        val signedPrekeyPublic: String,
        val signedPrekeySignature: String,
        val oneTimePrekeyId: Int?,
        val oneTimePrekeyPublic: String?,
        val sessionRecordBase64: String?,
    )

    fun getRemoteIdentity(userId: String, deviceId: String): RemoteIdentityLocal? =
        db.remoteIdentitiesQueries.selectRemoteIdentity(userId, deviceId).executeAsOneOrNull()?.let {
            RemoteIdentityLocal(
                userId = it.user_id,
                deviceId = it.device_id,
                identityKeyPublic = it.identity_key_public,
                identityFingerprint = it.identity_fingerprint,
                trustLevel = it.trust_level,
            )
        }

    fun recordRemoteIdentity(
        userId: String,
        deviceId: String,
        identityKeyPublic: String,
        identityFingerprint: String,
    ): RemoteIdentityDecision {
        val existing = getRemoteIdentity(userId, deviceId)
        if (existing == null) {
            db.remoteIdentitiesQueries.upsertRemoteIdentity(
                user_id = userId,
                device_id = deviceId,
                identity_key_public = identityKeyPublic,
                identity_fingerprint = identityFingerprint,
                trust_level = "trusted",
                updated_at = currentEpochSeconds(),
            )
            return RemoteIdentityDecision.TRUSTED
        }
        if (existing.identityFingerprint == identityFingerprint) {
            return RemoteIdentityDecision.MATCHED
        }
        db.remoteIdentitiesQueries.markRemoteIdentityChanged(
            updated_at = currentEpochSeconds(),
            user_id = userId,
            device_id = deviceId,
        )
        markSessionResetRequired(
            userId = userId,
            deviceId = deviceId,
            sessionState = "identity_changed",
            staleReason = STALE_REASON_REMOTE_IDENTITY_CHANGED,
        )
        return RemoteIdentityDecision.CHANGED
    }

    fun getSessionsForUser(userId: String): List<SignalSessionLocal> =
        db.signalSessionsQueries.selectAll().executeAsList()
            .filter { it.user_id == userId }
            .map {
                SignalSessionLocal(
                    userId = it.user_id,
                    deviceId = it.device_id,
                    protocolAddressName = it.protocol_address_name,
                    protocolDeviceSlot = it.protocol_device_slot.toInt(),
                    registrationId = it.registration_id.toInt(),
                    remoteIdentityFingerprint = it.remote_identity_fingerprint,
                    sessionState = it.session_state,
                    resetRequired = it.reset_required == 1L,
                    staleReason = it.stale_reason,
                    signedPrekeyId = it.signed_prekey_id.toInt(),
                    signedPrekeyPublic = it.signed_prekey_public,
                    signedPrekeySignature = it.signed_prekey_signature,
                    oneTimePrekeyId = it.one_time_prekey_id?.toInt(),
                    oneTimePrekeyPublic = it.one_time_prekey_public,
                    sessionRecordBase64 = it.session_record,
                )
            }

    fun getSession(userId: String, deviceId: String): SignalSessionLocal? =
        db.signalSessionsQueries.selectSessionByUserAndDevice(userId, deviceId).executeAsOneOrNull()?.let {
            SignalSessionLocal(
                userId = it.user_id,
                deviceId = it.device_id,
                protocolAddressName = it.protocol_address_name,
                protocolDeviceSlot = it.protocol_device_slot.toInt(),
                registrationId = it.registration_id.toInt(),
                remoteIdentityFingerprint = it.remote_identity_fingerprint,
                sessionState = it.session_state,
                resetRequired = it.reset_required == 1L,
                staleReason = it.stale_reason,
                signedPrekeyId = it.signed_prekey_id.toInt(),
                signedPrekeyPublic = it.signed_prekey_public,
                signedPrekeySignature = it.signed_prekey_signature,
                oneTimePrekeyId = it.one_time_prekey_id?.toInt(),
                oneTimePrekeyPublic = it.one_time_prekey_public,
                sessionRecordBase64 = it.session_record,
            )
        }

    fun getDeterministicSession(userId: String, deviceId: String): SignalSessionLocal? {
        val session = getSession(userId, deviceId) ?: return null
        if (session.protocolAddressName != SignalProtocol.addressName(userId)) return null
        if (session.protocolDeviceSlot != SignalProtocol.PROTOCOL_DEVICE_SLOT) return null
        return session
    }

    fun getUsableDeterministicSession(userId: String, deviceId: String): SignalSessionLocal? {
        val session = getDeterministicSession(userId, deviceId) ?: return null
        if (session.resetRequired) return null
        if (session.sessionState == "failed" || session.sessionState == "stale" || session.sessionState == "identity_changed") {
            return null
        }
        if (session.sessionRecordBase64.isNullOrBlank()) return null
        return session
    }

    fun upsertBootstrapReadySession(
        userId: String,
        deviceId: String,
        remoteIdentityFingerprint: String,
        registrationId: Int,
        sessionState: String,
        signedPrekeyId: Int,
        signedPrekeyPublic: String,
        signedPrekeySignature: String,
        oneTimePrekeyId: Int?,
        oneTimePrekeyPublic: String?,
        sessionRecordBase64: String?,
    ) {
        val now = currentEpochSeconds()
        db.signalSessionsQueries.upsertSignalSession(
            user_id = userId,
            device_id = deviceId,
            protocol_address_name = SignalProtocol.addressName(userId),
            protocol_device_slot = SignalProtocol.PROTOCOL_DEVICE_SLOT.toLong(),
            registration_id = registrationId.toLong(),
            remote_identity_fingerprint = remoteIdentityFingerprint,
            session_state = sessionState,
            reset_required = 0,
            stale_reason = null,
            signed_prekey_id = signedPrekeyId.toLong(),
            signed_prekey_public = signedPrekeyPublic,
            signed_prekey_signature = signedPrekeySignature,
            one_time_prekey_id = oneTimePrekeyId?.toLong(),
            one_time_prekey_public = oneTimePrekeyPublic,
            session_record = sessionRecordBase64,
            created_at = now,
            updated_at = now,
        )
    }

    fun markSessionResetRequired(
        userId: String,
        deviceId: String,
        sessionState: String = "stale",
        staleReason: String,
    ) {
        db.signalSessionsQueries.markSessionResetRequired(
            session_state = sessionState,
            stale_reason = staleReason,
            updated_at = currentEpochSeconds(),
            user_id = userId,
            device_id = deviceId,
        )
    }
}
