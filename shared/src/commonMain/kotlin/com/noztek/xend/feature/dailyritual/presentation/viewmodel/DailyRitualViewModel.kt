package com.noztek.xend.feature.dailyritual.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.feature.dailyritual.domain.usecase.GetDailyRitualOverviewUseCase
import com.noztek.xend.feature.dailyritual.presentation.state.DailyRitualUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailyRitualViewModel(
    private val getOverview: GetDailyRitualOverviewUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(DailyRitualUiState())
    val state: StateFlow<DailyRitualUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { getOverview() }
                .onSuccess { overview ->
                    _state.update { it.copy(isLoading = false, overview = overview) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, message = error.message ?: "Failed to load rituals") }
                }
        }
    }
}
