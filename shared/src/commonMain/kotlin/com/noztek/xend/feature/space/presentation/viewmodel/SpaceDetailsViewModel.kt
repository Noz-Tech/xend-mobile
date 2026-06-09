package com.noztek.xend.feature.space.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.feature.space.domain.usecase.GetRelationshipSpaceByIdUseCase
import com.noztek.xend.feature.space.presentation.state.SpaceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpaceDetailsViewModel(
    private val getRelationshipSpaceById: GetRelationshipSpaceByIdUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(SpaceUiState())
    val state: StateFlow<SpaceUiState> = _state.asStateFlow()

    fun load(spaceId: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { getRelationshipSpaceById(spaceId) }
                .onSuccess { item -> _state.update { it.copy(isLoading = false, defaultSpace = item) } }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, message = error.message ?: "Failed to load space") }
                }
        }
    }
}
