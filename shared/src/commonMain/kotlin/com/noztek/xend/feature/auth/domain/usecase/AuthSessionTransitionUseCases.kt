package com.noztek.xend.feature.auth.domain.usecase

import com.noztek.xend.core.realtime.RealtimeSessionCoordinator
import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import com.noztek.xend.feature.device.domain.usecase.DeviceKeysSyncer

class CompleteLoginSessionUseCase(
    private val deviceKeysSyncer: DeviceKeysSyncer,
    private val realtimeSessionCoordinator: RealtimeSessionCoordinator,
) {
    suspend operator fun invoke(session: AuthSessionModel) {
        runCatching { deviceKeysSyncer.sync(session.accessToken, session.deviceId) }
        realtimeSessionCoordinator.onLoginSuccess()
    }
}

class CompleteLogoutSessionUseCase(
    private val realtimeSessionCoordinator: RealtimeSessionCoordinator,
) {
    operator fun invoke() {
        realtimeSessionCoordinator.onLogout()
    }
}
