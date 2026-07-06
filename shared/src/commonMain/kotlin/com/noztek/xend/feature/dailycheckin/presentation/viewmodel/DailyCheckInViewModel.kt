package com.noztek.xend.feature.dailycheckin.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.feature.dailycheckin.domain.usecase.GetDailyCheckInOverviewUseCase
import com.noztek.xend.feature.dailycheckin.domain.usecase.SubmitDailyCheckInUseCase
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
                    _state.update { it.copy(isLoading = false, isSubmitting = false, overview = overview) }
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
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            overview = overview,
                        )
                    }
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
}
