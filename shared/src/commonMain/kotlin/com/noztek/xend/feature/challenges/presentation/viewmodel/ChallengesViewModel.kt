package com.noztek.xend.feature.challenges.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.feature.challenges.domain.model.ChallengeAudience
import com.noztek.xend.feature.challenges.domain.model.ChallengeCategory
import com.noztek.xend.feature.challenges.domain.usecase.GetChallengesOverviewUseCase
import com.noztek.xend.feature.challenges.presentation.state.ChallengesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChallengesViewModel(
    private val getOverview: GetChallengesOverviewUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(ChallengesUiState())
    val state: StateFlow<ChallengesUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { getOverview() }
                .onSuccess { overview ->
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            overview = overview,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = error.message ?: "Failed to load challenges.",
                        )
                    }
                }
        }
    }

    fun onAudienceSelected(audience: ChallengeAudience) {
        _state.update { it.copy(selectedAudience = audience) }
    }

    fun onCategorySelected(category: ChallengeCategory) {
        _state.update { it.copy(selectedCategory = category) }
    }
}
