package com.noztek.xend.core.session

import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao

class SessionInvalidationHandler(
    private val authSessionDao: AuthSessionDao,
    private val sessionEventBus: SessionEventBus,
) {
    fun onUnauthorized(reason: String = "access token is invalid") {
        val currentSession = authSessionDao.getCurrentSession() ?: return
        authSessionDao.clearSession()
        sessionEventBus.publish(
            type = SessionEventType.Expired,
            reason = reason.ifBlank { "Session expired" },
        )
    }
}
