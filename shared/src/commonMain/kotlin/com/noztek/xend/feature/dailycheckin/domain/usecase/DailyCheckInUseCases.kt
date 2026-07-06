package com.noztek.xend.feature.dailycheckin.domain.usecase

import com.noztek.xend.feature.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInMemberModel
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInMilestoneModel
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInMilestoneStatus
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInMoodTone
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInOverviewModel
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInStatusModel
import com.noztek.xend.feature.dailycheckin.domain.repository.DailyCheckInRepository
import com.noztek.xend.feature.space.data.local.dao.RelationshipSpaceMemberDao
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase

private const val DefaultDailyRewardPoints = 5

private data class DailyCheckInMilestoneSpec(
    val days: Int,
    val bonusPoints: Int,
)

private val milestoneSpecs = listOf(
    DailyCheckInMilestoneSpec(days = 3, bonusPoints = 10),
    DailyCheckInMilestoneSpec(days = 7, bonusPoints = 25),
    DailyCheckInMilestoneSpec(days = 14, bonusPoints = 50),
    DailyCheckInMilestoneSpec(days = 30, bonusPoints = 100),
    DailyCheckInMilestoneSpec(days = 60, bonusPoints = 200),
    DailyCheckInMilestoneSpec(days = 100, bonusPoints = 350),
)

class GetDailyCheckInOverviewUseCase(
    private val repository: DailyCheckInRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getCurrentUserProfile: GetCurrentUserProfileUseCase,
    private val memberDao: RelationshipSpaceMemberDao,
) {
    suspend operator fun invoke(): DailyCheckInOverviewModel {
        val space = requireNotNull(getDefaultRelationshipSpace()) { "No active space yet" }
        val status = repository.getStatus(space.relationshipSpaceId)
        return mapOverview(status)
    }

    suspend fun mapOverview(status: DailyCheckInStatusModel): DailyCheckInOverviewModel {
        val currentProfile = getCurrentUserProfile()
        val currentUserId = currentProfile?.userId
        val members = memberDao.getMembers(status.relationshipSpaceId)
        val partnerMember = currentUserId?.let { userId -> members.firstOrNull { it.userId != userId } }
            ?: members.firstOrNull()

        val partnerName = partnerMember?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: "Partner"

        val completedDaysCount = status.completedDaysCount
        val nextMilestone = milestoneSpecs.firstOrNull { it.days > completedDaysCount } ?: milestoneSpecs.last()
        val rewardPoints = status.dailyRewardPoints.takeIf { it > 0 } ?: DefaultDailyRewardPoints

        return DailyCheckInOverviewModel(
            relationshipSpaceId = status.relationshipSpaceId,
            dateLabel = status.checkInDate.toFriendlyDateLabel(),
            myCheckedIn = status.myCheckedIn,
            streakDays = status.currentStreak,
            streakSummary = status.toStreakSummary(),
            user = DailyCheckInMemberModel(
                title = "You",
                initials = currentProfile?.displayName.toInitials(fallback = "Y"),
                moodLabel = if (status.myCheckedIn) "Ready" else "Waiting",
                moodTone = if (status.myCheckedIn) DailyCheckInMoodTone.Happy else DailyCheckInMoodTone.Calm,
                checkedIn = status.myCheckedIn,
            ),
            partner = DailyCheckInMemberModel(
                title = partnerName,
                initials = partnerName.toInitials(fallback = "P"),
                moodLabel = if (status.partnerCheckedIn) "Ready" else "Waiting",
                moodTone = if (status.partnerCheckedIn) DailyCheckInMoodTone.Happy else DailyCheckInMoodTone.Calm,
                checkedIn = status.partnerCheckedIn,
            ),
            rewardPoints = rewardPoints,
            bothCheckedIn = status.allMembersCheckedIn,
            milestones = milestoneSpecs.map { spec ->
                DailyCheckInMilestoneModel(
                    days = spec.days,
                    bonusPoints = spec.bonusPoints,
                    status = when {
                        spec.days < completedDaysCount -> DailyCheckInMilestoneStatus.Reached
                        spec.days == completedDaysCount -> DailyCheckInMilestoneStatus.Current
                        else -> DailyCheckInMilestoneStatus.Locked
                    },
                )
            },
            nextMilestoneDays = nextMilestone.days,
            nextMilestoneRemainingDays = (nextMilestone.days - completedDaysCount).coerceAtLeast(0),
        )
    }

    private fun DailyCheckInStatusModel.toStreakSummary(): String {
        return when {
            currentStreak >= 2 -> "Both of you checked in for $currentStreak days in a row."
            allMembersCheckedIn -> "You both checked in today. Keep it going tomorrow."
            myCheckedIn -> "You're checked in. Your partner still needs to show up today."
            else -> "Show up together today to keep your bond growing."
        }
    }
}

class SubmitDailyCheckInUseCase(
    private val repository: DailyCheckInRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val getOverview: GetDailyCheckInOverviewUseCase,
) {
    suspend operator fun invoke(): DailyCheckInOverviewModel {
        val space = requireNotNull(getDefaultRelationshipSpace()) { "No active space yet" }
        repository.submitTodayCheckIn(space.relationshipSpaceId)
        runCatching { syncRelationshipSpaces() }
        return getOverview()
    }
}

private fun String?.toInitials(fallback: String): String {
    val source = this?.trim().orEmpty()
    if (source.isBlank()) return fallback

    val parts = source.split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> fallback
        parts.size == 1 -> parts.first().take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

private fun String.toFriendlyDateLabel(): String {
    val parts = split("-")
    if (parts.size != 3) return this

    val monthName = when (parts[1].toIntOrNull()) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> return this
    }
    val day = parts[2].toIntOrNull() ?: return this
    return "$monthName $day"
}
