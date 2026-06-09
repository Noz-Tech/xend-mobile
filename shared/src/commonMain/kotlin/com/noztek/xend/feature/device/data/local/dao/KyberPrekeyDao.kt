package com.noztek.xend.feature.device.data.local.dao

import com.noztek.xend.core.time.currentEpochSeconds
import org.noztek.Database

class KyberPrekeyDao(
    private val db: Database,
) {
    data class KyberPrekeyLocal(
        val deviceId: String,
        val keyId: Int,
        val publicKeyBase64: String,
        val privateKeyBase64: String,
        val signatureBase64: String,
        val recordBase64: String?,
        val uploadedAtEpochSeconds: Long?,
    )

    fun saveUploaded(
        deviceId: String,
        keyId: Int,
        publicKeyBase64: String,
        privateKeyBase64: String,
        signatureBase64: String,
        recordBase64: String?,
    ) {
        db.kyberPrekeysQueries.upsertKyberPrekey(
            device_id = deviceId,
            key_id = keyId.toLong(),
            public_key = publicKeyBase64,
            private_key = privateKeyBase64,
            signature = signatureBase64,
            record = recordBase64,
            uploaded_at = currentEpochSeconds(),
        )
    }

    fun getLatestForDevice(deviceId: String): KyberPrekeyLocal? =
        db.kyberPrekeysQueries.selectLatestByDevice(deviceId).executeAsOneOrNull()?.let {
            KyberPrekeyLocal(
                deviceId = it.device_id,
                keyId = it.key_id.toInt(),
                publicKeyBase64 = it.public_key,
                privateKeyBase64 = it.private_key,
                signatureBase64 = it.signature,
                recordBase64 = it.record,
                uploadedAtEpochSeconds = it.uploaded_at,
            )
        }
}
