package com.noztek.xend.feature.device.data.local.dao

import com.noztek.xend.core.time.currentEpochSeconds
import org.noztek.Database

data class OneTimePrekeyLocal(
    val keyId: Int,
    val publicKeyBase64: String,
    val privateKeyBase64: String,
)

class OneTimePrekeyDao(
    private val db: Database,
) {
    fun saveUploadedBatch(
        deviceId: String,
        prekeys: List<OneTimePrekeyLocal>,
    ) {
        val now = currentEpochSeconds()
        db.transaction {
            prekeys.forEach { prekey ->
                db.oneTimePrekeysQueries.insertOneTimePrekey(
                    device_id = deviceId,
                    key_id = prekey.keyId.toLong(),
                    public_key = prekey.publicKeyBase64,
                    private_key = prekey.privateKeyBase64,
                    status = "uploaded",
                    updated_at = now,
                )
            }
        }
    }

    fun getUploadedPrekeys(deviceId: String): List<OneTimePrekeyLocal> =
        db.oneTimePrekeysQueries.selectPendingUploadPrekeys(deviceId).executeAsList().map {
            OneTimePrekeyLocal(
                keyId = it.key_id.toInt(),
                publicKeyBase64 = it.public_key,
                privateKeyBase64 = it.private_key,
            )
        }

    fun getAllForDevice(deviceId: String): List<OneTimePrekeyLocal> =
        db.oneTimePrekeysQueries.selectAll(deviceId).executeAsList().map {
            OneTimePrekeyLocal(
                keyId = it.key_id.toInt(),
                publicKeyBase64 = it.public_key,
                privateKeyBase64 = it.private_key,
            )
        }

    fun countAvailableForDevice(deviceId: String): Int =
        db.oneTimePrekeysQueries.countAvailable(deviceId).executeAsOne().toInt()

    fun markUsed(deviceId: String, keyId: Int) {
        db.oneTimePrekeysQueries.markOneTimePrekeyUsed(
            updated_at = currentEpochSeconds(),
            device_id = deviceId,
            key_id = keyId.toLong(),
        )
    }
}
