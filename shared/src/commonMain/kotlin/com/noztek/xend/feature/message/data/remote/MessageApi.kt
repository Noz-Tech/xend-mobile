package com.noztek.xend.feature.message.data.remote

import com.noztek.xend.core.utils.errorMessageParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequestDto(
    @SerialName("client_message_id") val clientMessageId: String,
    @SerialName("message_type") val messageType: String,
    val ciphertext: String,
    @SerialName("reply_to_message_id") val replyToMessageId: String? = null,
    @SerialName("sender_timestamp") val senderTimestamp: Long? = null,
)

@Serializable
data class MessageDto(
    @SerialName("message_id") val messageId: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_user_id") val senderUserId: String,
    @SerialName("sender_device_id") val senderDeviceId: String,
    @SerialName("client_message_id") val clientMessageId: String,
    @SerialName("message_type") val messageType: String,
    val ciphertext: String,
    @SerialName("reply_to_message_id") val replyToMessageId: String? = null,
    @SerialName("sender_timestamp") val senderTimestamp: Long? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("receipt_user_id") val receiptUserId: String? = null,
    @SerialName("receipt_status") val receiptStatus: String? = null,
    @SerialName("delivered_at") val deliveredAt: Long? = null,
    @SerialName("read_at") val readAt: Long? = null,
    val reactions: List<MessageReactionDto> = emptyList(),
)

@Serializable
data class ReactMessageRequestDto(
    val emoji: String,
)

@Serializable
data class MessageReactionDto(
    @SerialName("message_id") val messageId: String,
    @SerialName("user_id") val userId: String,
    val emoji: String,
    @SerialName("removed_at") val removedAt: Long? = null,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
data class MessageListResponseDto(
    val items: List<MessageDto> = emptyList(),
)

class MessageApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun sendMessage(accessToken: String, conversationId: String, request: SendMessageRequestDto): MessageDto =
        execute {
            client.post(url("/v1/conversations/$conversationId/messages")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun syncMessages(accessToken: String, sinceEpochSeconds: Long?): List<MessageDto> =
        execute<MessageListResponseDto> {
            client.get(url("/v1/messages/sync")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                if (sinceEpochSeconds != null) {
                    url {
                        parameters.append("since", sinceEpochSeconds.toString())
                    }
                }
            }
        }.items

    suspend fun addReaction(accessToken: String, messageId: String, emoji: String) {
        execute<Unit> {
            client.post(url("/v1/messages/$messageId/reactions")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(ReactMessageRequestDto(emoji = emoji))
            }
        }
    }

    suspend fun removeReaction(accessToken: String, messageId: String, emoji: String) {
        execute<Unit> {
            client.delete(url("/v1/messages/$messageId/reactions")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                url { parameters.append("emoji", emoji) }
            }
        }
    }

    suspend fun markConversationRead(accessToken: String, conversationId: String) {
        execute<Unit> {
            client.post(url("/v1/conversations/$conversationId/read")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }
    }

    private suspend inline fun <reified T> execute(block: () -> HttpResponse): T {
        return try {
            val response = block()
            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw Exception(errorMessageParser(response.bodyAsText()))
            }
        } catch (e: ClientRequestException) {
            throw Exception(errorMessageParser(e.response.bodyAsText()))
        } catch (e: ServerResponseException) {
            throw Exception("Server error: ${e.response.status.value}")
        } catch (e: Exception) {
            throw Exception(e.message ?: "Unexpected network error")
        }
    }

    private fun url(path: String): String = "${baseUrl.trimEnd('/')}$path"
}
