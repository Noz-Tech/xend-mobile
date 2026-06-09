package com.noztek.xend.feature.device.domain.usecase

import com.noztek.xend.currentDeviceName
import com.noztek.xend.currentPlatformId
import com.noztek.xend.core.crypto.SignalBootstrapProvider
import com.noztek.xend.feature.device.data.local.dao.DeviceDao

class EnsureLocalSignalBootstrapUseCase(
    private val deviceDao: DeviceDao,
    private val signalBootstrapProvider: SignalBootstrapProvider,
) {
    operator fun invoke() {
        if (deviceDao.getCurrentSignalBootstrap() != null) return

        deviceDao.saveCurrentSignalBootstrap(
            deviceName = currentDeviceName(),
            platform = currentPlatformId(),
            signal = signalBootstrapProvider.create(),
        )
    }
}
