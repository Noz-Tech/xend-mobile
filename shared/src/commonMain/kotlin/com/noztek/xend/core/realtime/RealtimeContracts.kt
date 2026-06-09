package com.noztek.xend.core.realtime

data class PresenceStatus(
    val isOnline: Boolean,
    val lastSeenEpochSeconds: Long? = null,
)

data class RealtimeEvent(
    val type: String,
    val payload: Map<String, String> = emptyMap(),
)

interface PresenceGateway {
    suspend fun getUserPresence(accessToken: String, userId: String): PresenceStatus
}

interface RealtimeSessionCoordinator {
    fun onAppForeground()
    fun onAppBackground()
    fun onLoginSuccess()
    fun onLogout()
    fun sendTyping(conversationId: String, isTyping: Boolean)
}
