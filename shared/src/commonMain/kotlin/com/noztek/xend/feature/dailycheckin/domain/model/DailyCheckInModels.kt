package com.noztek.xend.feature.dailycheckin.domain.model

data class DailyCheckInOverviewModel(
    val relationshipSpaceId: String,
    val dateLabel: String,
    val myCheckedIn: Boolean,
    val streakDays: Int,
    val streakSummary: String,
    val user: DailyCheckInMemberModel,
    val partner: DailyCheckInMemberModel,
    val rewardPoints: Int,
    val bothCheckedIn: Boolean,
    val milestones: List<DailyCheckInMilestoneModel>,
    val nextMilestoneDays: Int,
    val nextMilestoneRemainingDays: Int,
)

data class DailyCheckInStatusModel(
    val relationshipSpaceId: String,
    val checkInDate: String,
    val myCheckedIn: Boolean,
    val partnerCheckedIn: Boolean,
    val allMembersCheckedIn: Boolean,
    val completedDaysCount: Int,
    val currentStreak: Int,
    val dailyRewardAwarded: Boolean,
    val dailyRewardPoints: Int,
    val milestoneAward: DailyCheckInMilestoneAwardModel?,
    val totalCheckInBondPointsEarned: Int,
)

data class DailyCheckInMilestoneAwardModel(
    val milestoneId: String,
    val completedDays: Int,
    val bonusPoints: Int,
    val title: String?,
    val description: String?,
)

data class DailyCheckInMemberModel(
    val title: String,
    val initials: String,
    val moodLabel: String,
    val moodTone: DailyCheckInMoodTone,
    val checkedIn: Boolean,
)

data class DailyCheckInMilestoneModel(
    val days: Int,
    val bonusPoints: Int,
    val status: DailyCheckInMilestoneStatus,
)

enum class DailyCheckInMoodTone {
    Calm,
    Happy,
}

enum class DailyCheckInMilestoneStatus {
    Reached,
    Current,
    Locked,
}
