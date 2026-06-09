package com.noztek.xend.feature.auth.data.impl

import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.feature.auth.data.local.dao.UserDao
import com.noztek.xend.feature.auth.data.remote.AuthApi
import com.noztek.xend.feature.auth.data.remote.model.LoginRequestDto
import com.noztek.xend.feature.auth.data.remote.model.RegisterRequestDto
import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import com.noztek.xend.feature.auth.domain.model.LoginParams
import com.noztek.xend.feature.auth.domain.model.RegisterParams
import com.noztek.xend.feature.auth.domain.model.RegisterResult
import com.noztek.xend.feature.auth.domain.model.UserProfileModel
import com.noztek.xend.feature.auth.domain.repository.AuthRepository
import com.noztek.xend.feature.device.data.local.dao.DeviceDao

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val authSessionDao: AuthSessionDao,
    private val deviceDao: DeviceDao,
    private val userDao: UserDao,
) : AuthRepository {
    override suspend fun register(params: RegisterParams): RegisterResult {
        val response = api.register(
            RegisterRequestDto(
                displayName = params.displayName,
                email = params.email.trim(),
                password = params.password,
                deviceName = params.deviceName,
                platform = params.platform,
                registrationId = params.registrationId,
                identityKeyPublic = params.identityKeyPublic,
            ),
        )

        return RegisterResult(
            userId = response.userId,
            email = response.email,
            requiresVerification = response.requiresVerification,
        )
    }

    override suspend fun verifyEmail(email: String, token: String) {
        api.verifyEmail(email.trim(), token.trim())
    }

    override suspend fun resendVerification(email: String) {
        api.resendVerification(email.trim())
    }

    override suspend fun login(params: LoginParams): AuthSessionModel {
        val auth = api.login(
            LoginRequestDto(
                email = params.email.trim(),
                password = params.password,
                deviceName = params.deviceName,
            ),
        )

        val session = authSessionDao.saveSession(
            userId = auth.userId,
            deviceId = auth.deviceId,
            accessToken = auth.accessToken,
            refreshToken = auth.refreshToken,
            expiresInSeconds = auth.expiresIn,
        )

        deviceDao.bindCurrentDeviceToSession(
            userId = auth.userId,
            deviceId = auth.deviceId,
        )

        val me = api.me(session.accessToken)
        userDao.saveUserProfile(
            UserProfileModel(
                userId = me.userId,
                displayName = me.displayName,
                email = me.email,
                avatarUrl = me.avatarUrl,
                identifier = me.identifier,
            ),
        )

        return session
    }

    override suspend fun refresh(): AuthSessionModel {
        val current = requireNotNull(authSessionDao.getCurrentSession()) {
            "No active session"
        }
        val refreshed = api.refresh(current.refreshToken)
        return authSessionDao.updateTokens(
            accessToken = refreshed.accessToken,
            refreshToken = refreshed.refreshToken,
            expiresInSeconds = refreshed.expiresIn,
        )
    }

    override suspend fun logout() {
        val current = authSessionDao.getCurrentSession()
        if (current != null) {
            runCatching { api.logout(current.accessToken) }
        }
        authSessionDao.clearSession()
    }

    override suspend fun getCurrentSession(): AuthSessionModel? = authSessionDao.getCurrentSession()

    override suspend fun getCurrentUserProfile(): UserProfileModel? {
        val userId = authSessionDao.getCurrentSession()?.userId ?: return null
        return userDao.getUserProfile(userId)
    }
}
