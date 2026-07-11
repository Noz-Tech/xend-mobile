package com.noztek.xend.feature.dailyritual.data.remote

import com.noztek.xend.core.ui.media.PickedImageData
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
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyRitualOverviewDto(
    @SerialName("relationship_space_id") val relationshipSpaceId: String,
    @SerialName("ritual_date") val ritualDate: String,
    @SerialName("today_ritual") val todayRitual: DailyRitualAssignedDto? = null,
    val history: List<DailyRitualAssignedDto> = emptyList(),
)

@Serializable
data class DailyRitualAssignedDto(
    @SerialName("assignment_id") val assignmentId: String,
    @SerialName("ritual_date") val ritualDate: String,
    val title: String,
    val description: String,
    val category: String,
    @SerialName("icon_key") val iconKey: String,
    @SerialName("reward_points") val rewardPoints: Int,
    @SerialName("submission_type") val submissionType: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("completion_rule") val completionRule: String,
    @SerialName("suggested_time") val suggestedTime: String? = null,
    val completed: Boolean,
    @SerialName("submitted_by_me") val submittedByMe: Boolean = false,
    @SerialName("submitted_count") val submittedCount: Int = 0,
    @SerialName("required_count") val requiredCount: Int = 1,
    @SerialName("can_submit") val canSubmit: Boolean = false,
)

@Serializable
data class SubmitDailyRitualRequestDto(
    @SerialName("text_response") val textResponse: String? = null,
)

class DailyRitualApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getOverview(accessToken: String, spaceId: String): DailyRitualOverviewDto =
        execute {
            client.get(url("/v1/relationship-spaces/$spaceId/daily-rituals")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

    suspend fun submit(
        accessToken: String,
        spaceId: String,
        assignmentId: String,
        textResponse: String? = null,
    ) {
        try {
            val response = client.post(url("/v1/relationship-spaces/$spaceId/daily-rituals/$assignmentId/submit")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(SubmitDailyRitualRequestDto(textResponse = textResponse))
            }
            if (response.status.value !in 200..299) {
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

    suspend fun submitImage(
        accessToken: String,
        spaceId: String,
        assignmentId: String,
        image: PickedImageData,
    ) {
        try {
            val response = client.post(url("/v1/relationship-spaces/$spaceId/daily-rituals/$assignmentId/submit")) {
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
            if (response.status.value !in 200..299) {
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
