package com.noztek.xend.feature.dailycheckin.presentation.state

import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInOverviewModel

data class DailyCheckInUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val overview: DailyCheckInOverviewModel? = null,
    val celebrationDialog: DailyCheckInCelebrationDialogModel? = null,
    val message: String? = null,
)

data class DailyCheckInCelebrationDialogModel(
    val key: String,
    val partnerName: String,
    val userInitials: String,
    val partnerInitials: String,
    val streakDays: Int,
    val dailyRewardPoints: Int,
    val totalRewardPoints: Int,
    val totalCheckInBondPointsEarned: Int,
    val milestoneDays: Int?,
    val milestoneTitle: String?,
    val milestoneBonusPoints: Int?,
)
