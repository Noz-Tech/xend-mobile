package com.noztek.xend.core.realtime

class AndroidPresenceGateway(
    private val presenceApi: PresenceApi,
) : PresenceGateway {
    override suspend fun getUserPresence(accessToken: String, userId: String): PresenceStatus {
        val presence = presenceApi.getUserPresence(accessToken, userId)
        return PresenceStatus(
            isOnline = presence.isOnline,
            lastSeenEpochSeconds = presence.lastSeen,
        )
    }
}
