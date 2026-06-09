package com.noztek.xend.feature.auth.data.local.dao

import com.noztek.xend.core.time.currentEpochSeconds
import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import org.noztek.Database

class AuthSessionDao(
    private val db: Database,
) {
    fun getCurrentSession(): AuthSessionModel? =
        db.authSessionQueries.selectLatestAuthSession().executeAsOneOrNull()?.let {
            AuthSessionModel(
                id = it.id,
                userId = it.user_id,
                deviceId = it.device_id,
                accessToken = it.access_token,
                refreshToken = it.refresh_token,
                accessExpiresAtEpochSeconds = it.access_expires_at,
                createdAtEpochSeconds = it.created_at,
            )
        }

    fun saveSession(
        userId: String,
        deviceId: String,
        accessToken: String,
        refreshToken: String,
        expiresInSeconds: Long,
    ): AuthSessionModel {
        val now = currentEpochSeconds()
        val accessExpiresAt = now + expiresInSeconds

        db.authSessionQueries.clearAuthSessions()
        db.authSessionQueries.insertAuthSession(
            user_id = userId,
            device_id = deviceId,
            access_token = accessToken,
            refresh_token = refreshToken,
            access_expires_at = accessExpiresAt,
            created_at = now,
        )

        return requireNotNull(getCurrentSession())
    }

    fun updateTokens(
        accessToken: String,
        refreshToken: String,
        expiresInSeconds: Long,
    ): AuthSessionModel {
        val current = requireNotNull(getCurrentSession())
        val accessExpiresAt = currentEpochSeconds() + expiresInSeconds

        db.authSessionQueries.updateAuthTokens(
            access_token = accessToken,
            refresh_token = refreshToken,
            access_expires_at = accessExpiresAt,
            id = current.id,
        )

        return requireNotNull(getCurrentSession())
    }

    fun clearSession() {
        db.authSessionQueries.clearAuthSessions()
    }
}
