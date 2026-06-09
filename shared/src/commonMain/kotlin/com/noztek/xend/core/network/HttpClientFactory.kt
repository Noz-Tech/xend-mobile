package com.noztek.xend.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(json: Json): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println(message)
            }
            level = LogLevel.BODY
        }
        install(WebSockets)
    }
}
