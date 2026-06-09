package com.noztek.xend.core.realtime

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceDto(
    @SerialName("user_id") val userId: String,
    @SerialName("is_online") val isOnline: Boolean,
    @SerialName("last_seen") val lastSeen: Long? = null,
)

class PresenceApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getUserPresence(accessToken: String, userId: String): PresenceDto {
        return client.get("${baseUrl.trimEnd('/')}/v1/users/$userId/presence") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
    }

    suspend fun markOffline(accessToken: String) {
        client.post("${baseUrl.trimEnd('/')}/v1/presence/offline") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }
}
