package com.noztek.xend.feature.challenges.data.remote

import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.core.ui.media.decodeRemoteImageBitmap
import com.noztek.xend.core.utils.errorMessageParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeTemplatesResponseDto(
    val items: List<ChallengeTemplateDto> = emptyList(),
)

@Serializable
data class ChallengeTemplateDto(
    @SerialName("template_id") val templateId: String,
    val slug: String,
    val title: String,
    val description: String,
    val category: String,
    @SerialName("icon_key") val iconKey: String,
    @SerialName("submission_type") val submissionType: String,
    @SerialName("min_level") val minLevel: Int,
    @SerialName("max_level") val maxLevel: Int? = null,
    @SerialName("default_points") val defaultPoints: Int,
    @SerialName("expiry_hours") val expiryHours: Int? = null,
    @SerialName("display_order") val displayOrder: Int,
)

@Serializable
data class ChallengesOverviewDto(
    @SerialName("relationship_space_id") val relationshipSpaceId: String,
    val incoming: List<ChallengeItemDto> = emptyList(),
    val sent: List<ChallengeItemDto> = emptyList(),
    val history: List<ChallengeItemDto> = emptyList(),
)

@Serializable
data class ChallengeItemDto(
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("relationship_space_id") val relationshipSpaceId: String,
    @SerialName("template_id") val templateId: String,
    val title: String,
    val description: String,
    val category: String,
    @SerialName("icon_key") val iconKey: String,
    @SerialName("submission_type") val submissionType: String,
    @SerialName("sender_user_id") val senderUserId: String,
    @SerialName("sender_display_name") val senderDisplayName: String,
    @SerialName("receiver_user_id") val receiverUserId: String,
    @SerialName("receiver_display_name") val receiverDisplayName: String,
    @SerialName("assigned_level") val assignedLevel: Int,
    @SerialName("reward_points") val rewardPoints: Int,
    val note: String? = null,
    val status: String,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("accepted_at") val acceptedAt: Long? = null,
    @SerialName("completed_at") val completedAt: Long? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
    @SerialName("submitted_by_me") val submittedByMe: Boolean = false,
    @SerialName("submission_text_response") val submissionTextResponse: String? = null,
    @SerialName("has_submission_image") val hasSubmissionImage: Boolean = false,
    @SerialName("can_accept") val canAccept: Boolean = false,
    @SerialName("can_decline") val canDecline: Boolean = false,
    @SerialName("can_complete") val canComplete: Boolean = false,
)

@Serializable
private data class CreateChallengeRequestDto(
    @SerialName("template_id") val templateId: String,
    val note: String? = null,
)

@Serializable
private data class CompleteChallengeRequestDto(
    @SerialName("text_response") val textResponse: String? = null,
)

class ChallengesApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getTemplates(accessToken: String, spaceId: String): List<ChallengeTemplateDto> =
        execute<ChallengeTemplatesResponseDto> {
            client.get(url("/v1/relationship-spaces/$spaceId/challenges/templates")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }.items

    suspend fun getOverview(accessToken: String, spaceId: String): ChallengesOverviewDto =
        execute {
            client.get(url("/v1/relationship-spaces/$spaceId/challenges")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

    suspend fun createChallenge(
        accessToken: String,
        spaceId: String,
        templateId: String,
        note: String?,
    ): ChallengesOverviewDto = execute {
        client.post(url("/v1/relationship-spaces/$spaceId/challenges")) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(
                CreateChallengeRequestDto(
                    templateId = templateId,
                    note = note,
                ),
            )
        }
    }

    suspend fun acceptChallenge(
        accessToken: String,
        spaceId: String,
        challengeId: String,
    ): ChallengesOverviewDto = execute {
        client.post(url("/v1/relationship-spaces/$spaceId/challenges/$challengeId/accept")) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    suspend fun declineChallenge(
        accessToken: String,
        spaceId: String,
        challengeId: String,
    ): ChallengesOverviewDto = execute {
        client.post(url("/v1/relationship-spaces/$spaceId/challenges/$challengeId/decline")) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    suspend fun completeChallenge(
        accessToken: String,
        spaceId: String,
        challengeId: String,
        textResponse: String? = null,
    ): ChallengesOverviewDto = execute {
        client.post(url("/v1/relationship-spaces/$spaceId/challenges/$challengeId/complete")) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(CompleteChallengeRequestDto(textResponse = textResponse))
        }
    }

    suspend fun completeChallengeImage(
        accessToken: String,
        spaceId: String,
        challengeId: String,
        image: PickedImageData,
    ): ChallengesOverviewDto = execute {
        client.post(url("/v1/relationship-spaces/$spaceId/challenges/$challengeId/complete")) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        appendInput(
                            key = "image",
                            headers = Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"${image.fileName}\"")
                                append(HttpHeaders.ContentType, image.mimeType)
                            },
                            size = image.bytes.size.toLong(),
                        ) {
                            ByteReadPacket(image.bytes)
                        }
                    },
                ),
            )
        }
    }

    suspend fun getSubmissionImage(
        accessToken: String,
        spaceId: String,
        challengeId: String,
    ) = try {
        val bytes: ByteArray = client.get(url("/v1/relationship-spaces/$spaceId/challenges/$challengeId/submission-image")) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body()
        require(bytes.isNotEmpty()) { "Challenge image is empty." }
        requireNotNull(decodeRemoteImageBitmap(bytes)) { "Unable to decode challenge image." }
    } catch (e: ClientRequestException) {
        throw Exception(errorMessageParser(e.response.bodyAsText()))
    } catch (e: ServerResponseException) {
        throw Exception("Server error: ${e.response.status.value}")
    } catch (e: Exception) {
        throw Exception(e.message ?: "Unexpected network error")
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
