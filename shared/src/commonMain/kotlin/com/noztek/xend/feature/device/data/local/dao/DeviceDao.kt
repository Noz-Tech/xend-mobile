package com.noztek.xend.feature.device.data.local.dao

import com.noztek.xend.core.crypto.SignalBootstrap
import com.noztek.xend.core.time.currentEpochSeconds
import org.noztek.Database

class DeviceDao(
    private val db: Database,
) {
    data class CurrentDeviceLocal(
        val deviceId: String,
        val userId: String,
        val deviceName: String,
        val platform: String,
        val registrationId: Int,
        val identityKeyPublicBase64: String,
        val identityKeyPrivateBase64: String,
        val updatedAtEpochSeconds: Long,
    )

    fun getCurrentDeviceId(): String? =
        db.deviceQueries.selectCurrentDevice().executeAsOneOrNull()?.device_id

    fun getCurrentDevice(): CurrentDeviceLocal? =
        db.deviceQueries.selectCurrentDevice().executeAsOneOrNull()?.let {
            CurrentDeviceLocal(
                deviceId = it.device_id,
                userId = it.user_id,
                deviceName = it.device_name,
                platform = it.platform,
                registrationId = it.registration_id.toInt(),
                identityKeyPublicBase64 = it.identity_key_public,
                identityKeyPrivateBase64 = it.identity_key_private,
                updatedAtEpochSeconds = it.updated_at,
            )
        }

    fun getCurrentSignalBootstrap(): SignalBootstrap? =
        getCurrentDevice()?.let {
            SignalBootstrap(
                registrationId = it.registrationId,
                identityKeyPublicBase64 = it.identityKeyPublicBase64,
                identityKeyPrivateBase64 = it.identityKeyPrivateBase64,
            )
        }

    fun saveCurrentSignalBootstrap(
        deviceName: String,
        platform: String,
        signal: SignalBootstrap,
    ) {
        val now = currentEpochSeconds()
        val current = db.deviceQueries.selectCurrentDevice().executeAsOneOrNull()

        db.deviceQueries.clearCurrentFlag()
        db.deviceQueries.upsertDevice(
            device_id = current?.device_id ?: localDeviceId(signal),
            user_id = current?.user_id ?: LOCAL_UNASSIGNED_USER_ID,
            device_name = deviceName,
            platform = platform,
            registration_id = signal.registrationId.toLong(),
            identity_key_public = signal.identityKeyPublicBase64,
            identity_key_private = signal.identityKeyPrivateBase64,
            is_current = 1,
            updated_at = now,
        )
    }

    fun bindCurrentDeviceToSession(userId: String, deviceId: String) {
        val current = getCurrentDevice() ?: return
        val now = currentEpochSeconds()

        db.deviceQueries.clearCurrentFlag()
        db.deviceQueries.upsertDevice(
            device_id = deviceId,
            user_id = userId,
            device_name = current.deviceName,
            platform = current.platform,
            registration_id = current.registrationId.toLong(),
            identity_key_public = current.identityKeyPublicBase64,
            identity_key_private = current.identityKeyPrivateBase64,
            is_current = 1,
            updated_at = now,
        )

        if (current.deviceId != deviceId) {
            db.deviceQueries.deleteDeviceById(current.deviceId)
        }
    }

    private fun localDeviceId(signal: SignalBootstrap): String {
        val fingerprint = signal.identityKeyPublicBase64
            .filter(Char::isLetterOrDigit)
            .take(24)
            .ifEmpty { signal.registrationId.toString() }
        return "local-$fingerprint"
    }

    private companion object {
        const val LOCAL_UNASSIGNED_USER_ID = "local_unassigned"
    }
}
