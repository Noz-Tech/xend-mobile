package com.noztek.xend.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class ApiHealthChecker(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun isOnline(): Boolean {
        return try {
            val response = client.get("$baseUrl/healthz")
            response.status.value == 200 && response.bodyAsText().contains("\"status\":\"ok\"")
        } catch (_: Exception) {
            false
        }
    }
}
