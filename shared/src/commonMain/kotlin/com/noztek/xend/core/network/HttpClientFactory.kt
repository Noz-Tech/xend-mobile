package com.noztek.xend.core.network

import com.noztek.xend.core.session.SessionInvalidationHandler
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(
    json: Json,
    sessionInvalidationHandler: SessionInvalidationHandler,
): HttpClient {
    return HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println(message)
            }
            level = LogLevel.BODY
        }
        HttpResponseValidator {
            handleResponseExceptionWithRequest { cause, request ->
                val response = (cause as? ResponseException)?.response ?: return@handleResponseExceptionWithRequest
                val isUnauthorized = response.status.value == 401
                val authHeader = request.headers[HttpHeaders.Authorization]
                val hasBearerToken = !authHeader.isNullOrBlank()
                val isLogoutRequest = request.url.encodedPath.endsWith("/v1/auth/logout")
                if (isUnauthorized && hasBearerToken && !isLogoutRequest) {
                    sessionInvalidationHandler.onUnauthorized()
                }
            }
        }
        install(WebSockets)
    }
}
