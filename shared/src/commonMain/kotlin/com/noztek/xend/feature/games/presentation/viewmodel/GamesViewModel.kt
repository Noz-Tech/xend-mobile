package com.noztek.xend.feature.games.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.feature.games.domain.model.GameCategory
import com.noztek.xend.feature.games.domain.usecase.GetGamesOverviewUseCase
import com.noztek.xend.feature.games.presentation.state.GamesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GamesViewModel(
    private val getOverview: GetGamesOverviewUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(GamesUiState())
    val state: StateFlow<GamesUiState> = _state.asStateFlow()

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
                            message = error.message ?: "Failed to load games.",
                        )
                    }
                }
        }
    }

    fun onCategorySelected(category: GameCategory) {
        _state.update { it.copy(selectedCategory = category) }
    }
}
