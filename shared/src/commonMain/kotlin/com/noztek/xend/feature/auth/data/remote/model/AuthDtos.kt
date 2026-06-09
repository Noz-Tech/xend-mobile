package com.noztek.xend.feature.auth.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    @SerialName("display_name") val displayName: String,
    val email: String,
    val password: String,
    @SerialName("device_name") val deviceName: String,
    val platform: String,
    @SerialName("registration_id") val registrationId: Int,
    @SerialName("identity_key_public") val identityKeyPublic: String,
)

@Serializable
data class RegisterResponseDto(
    @SerialName("user_id") val userId: String,
    val email: String,
    @SerialName("requires_verification") val requiresVerification: Boolean,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
    @SerialName("device_name") val deviceName: String,
)

@Serializable
data class VerifyEmailRequestDto(
    val email: String,
    val token: String,
)

@Serializable
data class ResendVerificationRequestDto(
    val email: String,
)

@Serializable
data class RefreshRequestDto(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class AuthResponseDto(
    @SerialName("user_id") val userId: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
)

@Serializable
data class TokenRefreshResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
)

@Serializable
data class UserProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("display_name") val displayName: String,
    val email: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val identifier: String,
)

@Serializable
data class ApiErrorDto(
    val code: String,
    val message: String,
)
