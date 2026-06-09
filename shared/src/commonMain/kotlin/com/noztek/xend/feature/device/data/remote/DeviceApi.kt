package com.noztek.xend.feature.device.data.remote

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
data class SignedPrekeyUploadRequest(
    val key_id: Int,
    val public_key: String,
    val signature: String,
)

@Serializable
data class KyberPrekeyUploadRequest(
    val key_id: Int,
    val public_key: String,
    val signature: String,
)

@Serializable
data class OneTimePrekeyUpload(
    val key_id: Int,
    val public_key: String,
)

@Serializable
data class OneTimePrekeyBatchUploadRequest(
    val prekeys: List<OneTimePrekeyUpload>,
)

@Serializable
data class PushTokenUploadRequest(
    val provider: String,
    val token: String,
)

@Serializable
data class RecipientPrekeysDto(
    @SerialName("user_id") val userId: String,
    val devices: List<RecipientDevicePrekeysDto> = emptyList(),
)

@Serializable
data class RecipientDevicePrekeysDto(
    @SerialName("device_id") val deviceId: String,
    @SerialName("registration_id") val registrationId: Int,
    @SerialName("identity_key_public") val identityKeyPublic: String,
    @SerialName("signed_prekey") val signedPrekey: RecipientSignedPrekeyDto,
    @SerialName("kyber_prekey") val kyberPrekey: RecipientKyberPrekeyDto,
    @SerialName("one_time_prekey") val oneTimePrekey: RecipientOneTimePrekeyDto? = null,
)

@Serializable
data class RecipientSignedPrekeyDto(
    @SerialName("key_id") val keyId: Int,
    @SerialName("public_key") val publicKey: String,
    @SerialName("signature") val signature: String,
)

@Serializable
data class RecipientKyberPrekeyDto(
    @SerialName("key_id") val keyId: Int,
    @SerialName("public_key") val publicKey: String,
    @SerialName("signature") val signature: String,
)

@Serializable
data class RecipientOneTimePrekeyDto(
    @SerialName("key_id") val keyId: Int,
    @SerialName("public_key") val publicKey: String,
)

class DeviceApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun uploadSignedPrekey(accessToken: String, deviceId: String, request: SignedPrekeyUploadRequest) {
        execute<Unit> {
            client.put(url("/v1/devices/$deviceId/signed-prekey")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun uploadOneTimePrekeys(accessToken: String, deviceId: String, request: OneTimePrekeyBatchUploadRequest) {
        execute<Unit> {
            client.post(url("/v1/devices/$deviceId/one-time-prekeys/batch")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun uploadKyberPrekey(accessToken: String, deviceId: String, request: KyberPrekeyUploadRequest) {
        execute<Unit> {
            client.put(url("/v1/devices/$deviceId/kyber-prekey")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun uploadPushToken(accessToken: String, deviceId: String, provider: String, token: String) {
        execute<Unit> {
            client.post(url("/v1/devices/$deviceId/push-token")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(PushTokenUploadRequest(provider = provider, token = token))
            }
        }
    }

    suspend fun getRecipientPrekeys(accessToken: String, userId: String): RecipientPrekeysDto =
        execute {
            client.get(url("/v1/users/$userId/prekeys")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
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
