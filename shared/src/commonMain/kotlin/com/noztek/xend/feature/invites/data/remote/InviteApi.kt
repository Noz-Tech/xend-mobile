package com.noztek.xend.feature.invites.data.remote

import com.noztek.xend.core.utils.errorMessageParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
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
data class CreateInviteRequestDto(
    val identifier: String,
    val note: String? = null,
)

@Serializable
data class CreateInviteResponseDto(
    val invite_id: String,
)

@Serializable
data class InviteInboxResponseDto(
    val items: List<InboxInviteDto>? = null,
)

@Serializable
data class InviteOutboxResponseDto(
    val items: List<OutboxInviteDto>? = null,
)

@Serializable
data class InboxInviteDto(
    @SerialName("InviteID") val inviteId: String,
    @SerialName("InviterUserID") val inviterUserId: String,
    @SerialName("InviterDisplayName") val inviterDisplayName: String,
    @SerialName("InviterIdentifier") val inviterIdentifier: String,
    @SerialName("Note") val note: String? = null,
    @SerialName("Status") val status: String,
    @SerialName("CreatedAt") val createdAt: String,
)

@Serializable
data class OutboxInviteDto(
    @SerialName("invite_id") val inviteId: String,
    @SerialName("invitee_identifier") val inviteeIdentifier: String,
    @SerialName("status") val status: String,
    @SerialName("note") val note: String? = null,
    @SerialName("created_at") val createdAt: Long,
)

class InviteApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun createInvite(accessToken: String, identifier: String, note: String?): String =
        execute<CreateInviteResponseDto> {
            client.post(url("/v1/relationship-invites/")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(CreateInviteRequestDto(identifier = identifier, note = note))
            }
        }.invite_id

    suspend fun inbox(accessToken: String): List<InboxInviteDto> =
        execute<InviteInboxResponseDto> {
            client.get(url("/v1/relationship-invites/inbox")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }.items ?: emptyList()

    suspend fun acceptInvite(accessToken: String, inviteId: String) {
        execute<Unit> {
            client.post(url("/v1/relationship-invites/$inviteId/accept")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }
    }

    suspend fun outbox(accessToken: String): List<OutboxInviteDto> =
        execute<InviteOutboxResponseDto> {
            client.get(url("/v1/relationship-invites/outbox")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }.items ?: emptyList()

    suspend fun declineInvite(accessToken: String, inviteId: String) {
        execute<Unit> {
            client.post(url("/v1/relationship-invites/$inviteId/decline")) {
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
