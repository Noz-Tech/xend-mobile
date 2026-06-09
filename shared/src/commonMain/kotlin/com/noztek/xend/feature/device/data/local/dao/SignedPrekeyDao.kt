package com.noztek.xend.feature.device.data.local.dao

import com.noztek.xend.core.time.currentEpochSeconds
import org.noztek.Database

class SignedPrekeyDao(
    private val db: Database,
) {
    data class SignedPrekeyLocal(
        val deviceId: String,
        val keyId: Int,
        val publicKeyBase64: String,
        val privateKeyBase64: String,
        val signatureBase64: String,
        val uploadedAtEpochSeconds: Long?,
    )

    fun saveUploaded(
        deviceId: String,
        keyId: Int,
        publicKeyBase64: String,
        privateKeyBase64: String,
        signatureBase64: String,
    ) {
        db.signedPrekeysQueries.upsertSignedPrekey(
            device_id = deviceId,
            key_id = keyId.toLong(),
            public_key = publicKeyBase64,
            private_key = privateKeyBase64,
            signature = signatureBase64,
            uploaded_at = currentEpochSeconds(),
        )
    }

    fun getLatestForDevice(deviceId: String): SignedPrekeyLocal? =
        db.signedPrekeysQueries.selectLatestSignedPrekeyForDevice(deviceId).executeAsOneOrNull()?.let {
            SignedPrekeyLocal(
                deviceId = it.device_id,
                keyId = it.key_id.toInt(),
                publicKeyBase64 = it.public_key,
                privateKeyBase64 = it.private_key,
                signatureBase64 = it.signature,
                uploadedAtEpochSeconds = it.uploaded_at,
            )
        }

    fun getAllForDevice(deviceId: String): List<SignedPrekeyLocal> =
        db.signedPrekeysQueries.selectAllSignedPrekeysForDevice(deviceId).executeAsList().map {
            SignedPrekeyLocal(
                deviceId = it.device_id,
                keyId = it.key_id.toInt(),
                publicKeyBase64 = it.public_key,
                privateKeyBase64 = it.private_key,
                signatureBase64 = it.signature,
                uploadedAtEpochSeconds = it.uploaded_at,
            )
        }
}
