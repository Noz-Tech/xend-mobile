package com.noztek.xend.core.realtime

import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RealtimePresenceManager(
    private val client: HttpClient,
    private val authSessionDao: AuthSessionDao,
    private val baseUrl: String,
    private val eventBus: RealtimeEventBus,
) : RealtimeSessionCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val socketMutex = Mutex()

    private var webSocket: DefaultClientWebSocketSession? = null
    private var connectedToken: String? = null
    private var parser = Json
    @Volatile
    private var appInForeground = false
    private var reconnectJob: Job? = null

    override fun onAppForeground() {
        appInForeground = true
        scope.launch { connectIfAuthenticated() }
    }

    override fun onAppBackground() {
        appInForeground = false
        reconnectJob?.cancel()
        reconnectJob = null
    }

    override fun onLoginSuccess() {
        appInForeground = true
        scope.launch { connectIfAuthenticated(forceReconnect = true) }
    }

    override fun onLogout() {
        appInForeground = false
        reconnectJob?.cancel()
        reconnectJob = null
        scope.launch { disconnect("logout") }
    }

    override fun sendTyping(conversationId: String, isTyping: Boolean) {
        scope.launch {
            socketMutex.withLock {
                webSocket?.send(
                    Frame.Text(
                        """{"type":"typing","payload":{"conversation_id":"$conversationId","is_typing":"$isTyping"}}""",
                    ),
                )
            }
        }
    }

    private suspend fun connectIfAuthenticated(forceReconnect: Boolean = false) {
        val session = authSessionDao.getCurrentSession() ?: return

        socketMutex.withLock {
            if (!forceReconnect && webSocket != null && connectedToken == session.accessToken) {
                return
            }

            closeSocketLocked("reconnect")

            runCatching {
                client.webSocketSession {
                    url(httpToWs("${baseUrl.trimEnd('/')}/v1/ws?access_token=${session.accessToken}"))
                }
            }.onSuccess { socket ->
                webSocket = socket
                connectedToken = session.accessToken
                reconnectJob?.cancel()
                reconnectJob = null
                eventBus.publish("realtime_connected")
                scope.launch { readSocketMessages(socket, session.accessToken) }
            }.onFailure {
                webSocket = null
                connectedToken = null
                requestReconnect()
            }
        }
    }

    private suspend fun readSocketMessages(
        socket: DefaultClientWebSocketSession,
        accessToken: String,
    ) {
        try {
            for (frame in socket.incoming) {
                val text = (frame as? Frame.Text)?.readText() ?: continue
                handleRealtimeMessage(text)
            }
        } catch (_: ClosedReceiveChannelException) {
            // Normal close path.
        } catch (_: Throwable) {
            // Reconnect is driven by later lifecycle or auth events.
        } finally {
            socketMutex.withLock {
                if (webSocket === socket && connectedToken == accessToken) {
                    webSocket = null
                    connectedToken = null
                }
            }
            requestReconnect()
        }
    }

    private fun handleRealtimeMessage(text: String) {
        runCatching {
            val root = parser.parseToJsonElement(text).jsonObject
            val type = root["type"]?.jsonPrimitive?.content
            val payload = root["payload"]
                ?.jsonObject
                ?.mapNotNull { (key, value) -> value.jsonPrimitive.contentOrNull?.let { key to it } }
                ?.toMap()
                .orEmpty()
            if (!type.isNullOrBlank()) {
                eventBus.publish(type, payload)
            }
        }
    }

    private suspend fun disconnect(reason: String) {
        socketMutex.withLock {
            closeSocketLocked(reason)
        }
    }

    private suspend fun closeSocketLocked(reason: String) {
        val socket = webSocket
        webSocket = null
        connectedToken = null
        runCatching {
            socket?.close(CloseReason(CloseReason.Codes.NORMAL, reason))
        }
    }

    private fun requestReconnect() {
        if (!appInForeground) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(RECONNECT_DELAY_MS)
            connectIfAuthenticated()
        }
    }

    private fun httpToWs(url: String): String {
        return when {
            url.startsWith("https://") -> "wss://${url.removePrefix("https://")}"
            url.startsWith("http://") -> "ws://${url.removePrefix("http://")}"
            else -> url
        }
    }

    private companion object {
        const val RECONNECT_DELAY_MS = 2_000L
    }
}
