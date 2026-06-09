package com.noztek.xend.feature.device.domain.usecase

interface DeviceKeysSyncer {
    suspend fun sync(accessToken: String, deviceId: String)

    suspend fun syncIfNeeded(accessToken: String, deviceId: String) {
        sync(accessToken, deviceId)
    }
}
