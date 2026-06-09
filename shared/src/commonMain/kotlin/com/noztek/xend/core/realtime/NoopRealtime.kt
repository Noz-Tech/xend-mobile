package com.noztek.xend.core.realtime

class NoopPresenceGateway : PresenceGateway {
    override suspend fun getUserPresence(accessToken: String, userId: String): PresenceStatus = PresenceStatus(false, null)
}

class NoopRealtimeSessionCoordinator : RealtimeSessionCoordinator {
    override fun onAppForeground() = Unit
    override fun onAppBackground() = Unit
    override fun onLoginSuccess() = Unit
    override fun onLogout() = Unit
    override fun sendTyping(conversationId: String, isTyping: Boolean) = Unit
}
