package com.noztek.xend.feature.auth.domain.usecase

import com.noztek.xend.currentPlatformId
import com.noztek.xend.feature.auth.domain.model.RegisterParams
import com.noztek.xend.feature.device.data.local.dao.DeviceDao

class RegisterWithEmailUseCase(
    private val deviceDao: DeviceDao,
    private val registerUser: RegisterUserUseCase,
) {
    suspend operator fun invoke(
        displayName: String,
        email: String,
        password: String,
        deviceName: String,
    ): String {
        val currentDevice = requireNotNull(deviceDao.getCurrentDevice()) {
            "Missing local device identity. Restart app and try again."
        }
        registerUser(
            RegisterParams(
                displayName = displayName,
                email = email,
                password = password,
                deviceName = deviceName,
                platform = currentPlatformId(),
                registrationId = currentDevice.registrationId,
                identityKeyPublic = currentDevice.identityKeyPublicBase64,
            ),
        )
        return email.trim().lowercase()
    }
}

class VerifyEmailCodeUseCase(
    private val verifyEmail: VerifyEmailUseCase,
) {
    suspend operator fun invoke(email: String, token: String) {
        verifyEmail(email, token)
    }
}

class ResendVerificationCodeUseCase(
    private val resendVerification: ResendVerificationUseCase,
) {
    suspend operator fun invoke(email: String) {
        resendVerification(email)
    }
}
