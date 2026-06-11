package com.noztek.xend.feature.auth.domain.model

data class RegisterParams(
    val displayName: String,
    val email: String,
    val password: String,
    val deviceName: String,
    val platform: String,
    val registrationId: Int,
    val identityKeyPublic: String,
)

data class RegisterResult(
    val userId: String,
    val email: String,
    val requiresVerification: Boolean,
)

data class LoginParams(
    val email: String,
    val password: String,
    val deviceName: String,
)

enum class PendingAuthFlowStep {
    VERIFY_EMAIL,
}

data class PendingAuthFlowModel(
    val step: PendingAuthFlowStep,
    val email: String,
    val createdAtEpochSeconds: Long,
)

data class AuthSessionModel(
    val id: Long,
    val userId: String,
    val deviceId: String,
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAtEpochSeconds: Long,
    val createdAtEpochSeconds: Long,
)

data class UserProfileModel(
    val userId: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val identifier: String,
)
