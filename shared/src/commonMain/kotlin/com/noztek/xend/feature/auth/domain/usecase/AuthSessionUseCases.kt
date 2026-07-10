package com.noztek.xend.feature.auth.domain.usecase

import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import com.noztek.xend.feature.auth.domain.model.UserProfileModel
import com.noztek.xend.feature.auth.domain.repository.AuthRepository

class VerifyEmailUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, token: String) = repository.verifyEmail(email, token)
}

class ResendVerificationUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String) = repository.resendVerification(email)
}

class RefreshSessionUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AuthSessionModel = repository.refresh()
}

class LogoutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.logout()
}

class GetCurrentSessionUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AuthSessionModel? = repository.getCurrentSession()
}

class GetCurrentUserProfileUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): UserProfileModel? = repository.getCurrentUserProfile()
}

class HasAnyLocalUserProfileUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean = repository.hasAnyLocalUserProfile()
}
