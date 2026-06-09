package com.noztek.xend.feature.device.domain.usecase

interface SignalSessionBootstrapper {
    suspend fun bootstrap(targetUserIds: Collection<String>? = null)
}
