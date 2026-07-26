package com.noztek.xend.feature.dailycheckin.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInOverviewModel
import com.noztek.xend.feature.dailycheckin.domain.usecase.GetDailyCheckInOverviewUseCase
import com.noztek.xend.feature.dailycheckin.domain.usecase.SubmitDailyCheckInUseCase
import com.noztek.xend.feature.dailycheckin.presentation.state.DailyCheckInCelebrationDialogModel
import com.noztek.xend.feature.dailycheckin.presentation.state.DailyCheckInUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailyCheckInViewModel(
    private val getOverview: GetDailyCheckInOverviewUseCase,
    private val submitDailyCheckIn: SubmitDailyCheckInUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(DailyCheckInUiState())
    private var lastShownCelebrationKey: String? = null
    val state: StateFlow<DailyCheckInUiState> = _state.asStateFlow()

    init {
        refresh()
        scope.launch {
            realtimeSignals.spaceRefreshTick.collect {
                if (it > 0L) refresh()
            }
        }
    }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { getOverview() }
                .onSuccess { overview ->
                    applyOverview(overview, isLoading = false, isSubmitting = false)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSubmitting = false,
                            message = error.message ?: "Failed to load check-in",
                        )
                    }
                }
        }
    }

    fun submit(onSuccess: () -> Unit = {}) {
        val current = _state.value
        if (current.isSubmitting || current.overview?.myCheckedIn == true) return

        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { submitDailyCheckIn() }
                .onSuccess { overview ->
                    applyOverview(overview, isLoading = false, isSubmitting = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = error.message ?: "Failed to check in",
                        )
                    }
                }
        }
    }

    fun dismissCelebrationDialog() {
        _state.update { it.copy(celebrationDialog = null) }
    }

    private fun applyOverview(
        overview: DailyCheckInOverviewModel,
        isLoading: Boolean,
        isSubmitting: Boolean,
    ) {
        val celebration = overview.toCelebrationDialog()
        val shouldShowCelebration = celebration != null && celebration.key != lastShownCelebrationKey
        if (shouldShowCelebration) {
            lastShownCelebrationKey = celebration.key
        }

        _state.update { current ->
            current.copy(
                isLoading = isLoading,
                isSubmitting = isSubmitting,
                overview = overview,
                celebrationDialog = when {
                    shouldShowCelebration -> celebration
                    current.celebrationDialog != null -> current.celebrationDialog
                    else -> null
                },
            )
        }
    }

    private fun DailyCheckInOverviewModel.toCelebrationDialog(): DailyCheckInCelebrationDialogModel? {
        if (!bothCheckedIn || !dailyRewardAwarded) return null

        val milestone = milestoneAward
        val totalRewardPoints = rewardPoints + (milestone?.bonusPoints ?: 0)
        val key = buildString {
            append(relationshipSpaceId)
            append(':')
            append(checkInDate)
            append(":daily:")
            append(rewardPoints)
            append(":milestone:")
            append(milestone?.milestoneId ?: "none")
        }

        return DailyCheckInCelebrationDialogModel(
            key = key,
            partnerName = partner.title,
            userInitials = user.initials,
            partnerInitials = partner.initials,
            streakDays = streakDays,
            dailyRewardPoints = rewardPoints,
            totalRewardPoints = totalRewardPoints,
            totalCheckInBondPointsEarned = totalCheckInBondPointsEarned,
            milestoneDays = milestone?.completedDays,
            milestoneTitle = milestone?.title,
            milestoneBonusPoints = milestone?.bonusPoints,
        )
    }
}
