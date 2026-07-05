package com.noztek.xend.feature.space.data.remote

import com.noztek.xend.core.utils.errorMessageParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpaceListResponseDto(
    val items: List<SpaceDto> = emptyList(),
)

@Serializable
data class SpaceDto(
    @SerialName("relationship_space_id") val relationshipSpaceId: String,
    @SerialName("conversation_id") val conversationId: String,
    val name: String? = null,
    @SerialName("created_by_user_id") val createdByUserId: String,
    @SerialName("current_level") val currentLevel: Int = 1,
    @SerialName("current_level_name") val currentLevelName: String = "Tease",
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("access_hint") val accessHint: String? = null,
    @SerialName("access_configured") val accessConfigured: Boolean = false,
    @SerialName("archived_at") val archivedAt: Long? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
data class ConfigureSpaceAccessRequestDto(
    val passphrase: String,
    val hint: String? = null,
)

@Serializable
data class UnlockSpaceRequestDto(
    val passphrase: String,
)

@Serializable
data class LevelProgressListResponseDto(
    val items: List<LevelProgressDto> = emptyList(),
)

@Serializable
data class SpaceMembersResponseDto(
    val items: List<SpaceMemberDto> = emptyList(),
)

@Serializable
data class SpaceMemberDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("identifier") val identifier: String,
)

@Serializable
data class LevelProgressDto(
    @SerialName("relationship_space_id") val relationshipSpaceId: String,
    val level: Int,
    @SerialName("required_points") val requiredPoints: Int,
    @SerialName("current_points") val currentPoints: Int,
    @SerialName("unlocked_at") val unlockedAt: Long? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

class SpaceApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getSpaces(accessToken: String): List<SpaceDto> =
        execute<SpaceListResponseDto> {
            client.get(url("/v1/relationship-spaces")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }.items

    suspend fun getLevelProgress(accessToken: String, spaceId: String): List<LevelProgressDto> =
        execute<LevelProgressListResponseDto> {
            client.get(url("/v1/relationship-spaces/$spaceId/level-progress")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }.items

    suspend fun getSpaceMembers(accessToken: String, spaceId: String): List<SpaceMemberDto> =
        execute<SpaceMembersResponseDto> {
            client.get(url("/v1/relationship-spaces/$spaceId/members")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }.items

    suspend fun setDefaultSpace(accessToken: String, spaceId: String) {
        execute<Unit> {
            client.put(url("/v1/relationship-spaces/$spaceId/default")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }
    }

    suspend fun configureSpaceAccess(accessToken: String, spaceId: String, passphrase: String, hint: String?) {
        execute<Unit> {
            client.put(url("/v1/relationship-spaces/$spaceId/access-lock")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(ConfigureSpaceAccessRequestDto(passphrase = passphrase, hint = hint))
            }
        }
    }

    suspend fun unlockSpace(accessToken: String, passphrase: String): SpaceDto =
        execute {
            client.post(url("/v1/relationship-spaces/unlock")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(UnlockSpaceRequestDto(passphrase = passphrase))
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
