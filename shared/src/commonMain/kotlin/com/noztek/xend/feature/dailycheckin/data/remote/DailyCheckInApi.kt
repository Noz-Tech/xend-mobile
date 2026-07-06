package com.noztek.xend.feature.dailycheckin.data.remote

import com.noztek.xend.core.utils.errorMessageParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyCheckInStatusDto(
    @SerialName("relationship_space_id") val relationshipSpaceId: String,
    @SerialName("checkin_date") val checkInDate: String,
    @SerialName("my_checked_in") val myCheckedIn: Boolean,
    @SerialName("partner_checked_in") val partnerCheckedIn: Boolean,
    @SerialName("all_members_checked_in") val allMembersCheckedIn: Boolean,
    @SerialName("completed_days_count") val completedDaysCount: Int,
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("daily_reward_awarded") val dailyRewardAwarded: Boolean,
    @SerialName("daily_reward_points") val dailyRewardPoints: Int,
    @SerialName("milestone_award") val milestoneAward: DailyCheckInMilestoneAwardDto? = null,
    @SerialName("total_checkin_bond_points_earned") val totalCheckInBondPointsEarned: Int,
)

@Serializable
data class DailyCheckInMilestoneAwardDto(
    @SerialName("milestone_id") val milestoneId: String,
    @SerialName("completed_days") val completedDays: Int,
    @SerialName("bonus_points") val bonusPoints: Int,
    val title: String? = null,
    val description: String? = null,
)

class DailyCheckInApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getTodayStatus(accessToken: String, spaceId: String): DailyCheckInStatusDto =
        execute {
            client.get(url("/v1/relationship-spaces/$spaceId/daily-checkin")) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

    suspend fun submitTodayCheckIn(accessToken: String, spaceId: String): DailyCheckInStatusDto =
        execute {
            client.post(url("/v1/relationship-spaces/$spaceId/daily-checkin")) {
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
