package com.noztek.xend.feature.device.domain.usecase

import android.util.Log
import com.noztek.xend.core.crypto.decodeBase64
import com.noztek.xend.core.crypto.encodeBase64
import com.noztek.xend.core.notify.PushTokenProvider
import com.noztek.xend.feature.device.data.local.dao.DeviceDao
import com.noztek.xend.feature.device.data.local.dao.KyberPrekeyDao
import com.noztek.xend.feature.device.data.local.dao.OneTimePrekeyDao
import com.noztek.xend.feature.device.data.local.dao.OneTimePrekeyLocal
import com.noztek.xend.feature.device.data.local.dao.SignedPrekeyDao
import com.noztek.xend.feature.device.data.remote.DeviceApi
import com.noztek.xend.feature.device.data.remote.KyberPrekeyUploadRequest
import com.noztek.xend.feature.device.data.remote.OneTimePrekeyBatchUploadRequest
import com.noztek.xend.feature.device.data.remote.OneTimePrekeyUpload
import com.noztek.xend.feature.device.data.remote.SignedPrekeyUploadRequest
import kotlin.random.Random
import kotlinx.coroutines.delay
import org.signal.libsignal.protocol.ecc.ECPrivateKey
import org.signal.libsignal.protocol.kem.KEMKeyPair
import org.signal.libsignal.protocol.kem.KEMKeyType
import org.signal.libsignal.protocol.state.KyberPreKeyRecord

class SyncDeviceKeysUseCase(
    private val deviceDao: DeviceDao,
    private val signedPrekeyDao: SignedPrekeyDao,
    private val kyberPrekeyDao: KyberPrekeyDao,
    private val oneTimePrekeyDao: OneTimePrekeyDao,
    private val deviceApi: DeviceApi,
    private val pushTokenProvider: PushTokenProvider,
) {
    suspend operator fun invoke(accessToken: String, deviceId: String) {
        val bootstrap = requireNotNull(deviceDao.getCurrentSignalBootstrap()) {
            "Missing local device identity"
        }

        val identityPrivate = ECPrivateKey(decodeBase64(bootstrap.identityKeyPrivateBase64))

        val signedPrekeyPrivate = ECPrivateKey.generate()
        val signedPrekeyPublicBytes = signedPrekeyPrivate.getPublicKey().serialize()
        val signedPrekeySignature = identityPrivate.calculateSignature(signedPrekeyPublicBytes)
        val signedPrekeyId = Random.nextInt(1, Int.MAX_VALUE)

        val signedRequest = SignedPrekeyUploadRequest(
            key_id = signedPrekeyId,
            public_key = encodeBase64(signedPrekeyPublicBytes),
            signature = encodeBase64(signedPrekeySignature),
        )

        deviceApi.uploadSignedPrekey(accessToken, deviceId, signedRequest)
        signedPrekeyDao.saveUploaded(
            deviceId = deviceId,
            keyId = signedPrekeyId,
            publicKeyBase64 = signedRequest.public_key,
            privateKeyBase64 = encodeBase64(signedPrekeyPrivate.serialize()),
            signatureBase64 = signedRequest.signature,
        )

        val kyberPair = KEMKeyPair.generate(KEMKeyType.KYBER_1024)
        val kyberKeyId = Random.nextInt(1, Int.MAX_VALUE)
        val kyberPublicBytes = kyberPair.publicKey.serialize()
        val kyberSignature = identityPrivate.calculateSignature(kyberPublicBytes)
        val kyberRequest = KyberPrekeyUploadRequest(
            key_id = kyberKeyId,
            public_key = encodeBase64(kyberPublicBytes),
            signature = encodeBase64(kyberSignature),
        )
        val kyberRecord = KyberPreKeyRecord(
            kyberKeyId,
            System.currentTimeMillis(),
            kyberPair,
            kyberSignature,
        )

        deviceApi.uploadKyberPrekey(accessToken, deviceId, kyberRequest)
        kyberPrekeyDao.saveUploaded(
            deviceId = deviceId,
            keyId = kyberKeyId,
            publicKeyBase64 = kyberRequest.public_key,
            privateKeyBase64 = encodeBase64(kyberPair.secretKey.serialize()),
            signatureBase64 = kyberRequest.signature,
            recordBase64 = encodeBase64(kyberRecord.serialize()),
        )

        replenishOneTimePrekeysIfNeeded(accessToken, deviceId)

        var pushToken: String? = null
        repeat(3) { attempt ->
            pushToken = pushTokenProvider.getTokenOrNull()
            if (!pushToken.isNullOrBlank()) return@repeat
            Log.w(TAG, "Push token unavailable on attempt=${attempt + 1}")
            delay(1000)
        }
        if (!pushToken.isNullOrBlank()) {
            deviceApi.uploadPushToken(accessToken, deviceId, provider = "fcm", token = requireNotNull(pushToken))
            Log.i(TAG, "Push token uploaded for device_id=$deviceId")
        } else {
            Log.w(TAG, "Push token still unavailable after retries, skipping upload")
        }
    }

    private suspend fun replenishOneTimePrekeysIfNeeded(accessToken: String, deviceId: String) {
        val available = oneTimePrekeyDao.countAvailableForDevice(deviceId)
        if (available >= MIN_ONE_TIME_PREKEY_THRESHOLD) return

        val countToGenerate = TARGET_ONE_TIME_PREKEY_COUNT - available
        if (countToGenerate <= 0) return

        val start = Random.nextInt(1, 900_000)
        val oneTimeLocal = (0 until countToGenerate).map { index ->
            val key = ECPrivateKey.generate()
            OneTimePrekeyLocal(
                keyId = start + index,
                publicKeyBase64 = encodeBase64(key.getPublicKey().serialize()),
                privateKeyBase64 = encodeBase64(key.serialize()),
            )
        }

        val batchRequest = OneTimePrekeyBatchUploadRequest(
            prekeys = oneTimeLocal.map {
                OneTimePrekeyUpload(
                    key_id = it.keyId,
                    public_key = it.publicKeyBase64,
                )
            },
        )

        deviceApi.uploadOneTimePrekeys(accessToken, deviceId, batchRequest)
        oneTimePrekeyDao.saveUploadedBatch(deviceId, oneTimeLocal)
    }

    private companion object {
        const val TAG = "SyncDeviceKeysUseCase"
        const val TARGET_ONE_TIME_PREKEY_COUNT = 20
        const val MIN_ONE_TIME_PREKEY_THRESHOLD = 5
    }
}
