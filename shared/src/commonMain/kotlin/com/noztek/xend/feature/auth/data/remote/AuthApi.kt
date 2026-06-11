package com.noztek.xend.feature.auth.data.remote

import com.noztek.xend.core.utils.errorMessageParser
import com.noztek.xend.feature.auth.data.remote.model.ApiErrorDto
import com.noztek.xend.feature.auth.data.remote.model.AuthResponseDto
import com.noztek.xend.feature.auth.data.remote.model.LoginRequestDto
import com.noztek.xend.feature.auth.data.remote.model.RefreshRequestDto
import com.noztek.xend.feature.auth.data.remote.model.RegisterRequestDto
import com.noztek.xend.feature.auth.data.remote.model.RegisterResponseDto
import com.noztek.xend.feature.auth.data.remote.model.ResendVerificationRequestDto
import com.noztek.xend.feature.auth.data.remote.model.TokenRefreshResponseDto
import com.noztek.xend.feature.auth.data.remote.model.UserProfileDto
import com.noztek.xend.feature.auth.data.remote.model.VerifyEmailRequestDto
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
import kotlinx.serialization.json.Json

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val errorJson = Json { ignoreUnknownKeys = true }

    suspend fun register(request: RegisterRequestDto): RegisterResponseDto =
        execute {
            client.post(url("/v1/auth/register")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun verifyEmail(email: String, token: String) {
        execute<Unit> {
            client.post(url("/v1/auth/verify-email")) {
                contentType(ContentType.Application.Json)
                setBody(VerifyEmailRequestDto(email, token))
            }
        }
    }

    suspend fun resendVerification(email: String) {
        execute<Unit> {
            client.post(url("/v1/auth/resend-verification")) {
                contentType(ContentType.Application.Json)
                setBody(ResendVerificationRequestDto(email))
            }
        }
    }

    suspend fun login(request: LoginRequestDto): AuthResponseDto =
        execute {
            client.post(url("/v1/auth/login")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun refresh(refreshToken: String): TokenRefreshResponseDto =
        execute {
            client.post(url("/v1/auth/refresh")) {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequestDto(refreshToken))
            }
        }

    suspend fun logout(accessToken: String) {
        execute<Unit> {
            client.post(url("/v1/auth/logout")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
            }
        }
    }

    suspend fun me(accessToken: String): UserProfileDto =
        execute {
            client.get(url("/v1/users/me")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

    private suspend inline fun <reified T> execute(block: () -> HttpResponse): T {
        return try {
            val response = block()
            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw parseApiException(response.bodyAsText())
            }
        } catch (e: ClientRequestException) {
            throw parseApiException(e.response.bodyAsText())
        } catch (e: ServerResponseException) {
            throw Exception("Server error: ${e.response.status.value}")
        } catch (e: AuthApiException) {
            throw e
        } catch (e: Exception) {
            throw Exception(e.message ?: "Unexpected network error")
        }
    }

    private fun url(path: String): String = "${baseUrl.trimEnd('/')}$path"

    private fun parseApiException(errorText: String): AuthApiException {
        val dto = runCatching { errorJson.decodeFromString<ApiErrorDto>(errorText) }.getOrNull()
        return AuthApiException(
            code = dto?.code,
            message = dto?.message ?: errorMessageParser(errorText),
        )
    }
}
