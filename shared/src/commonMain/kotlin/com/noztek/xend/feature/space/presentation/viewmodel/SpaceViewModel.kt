package com.noztek.xend.feature.space.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.feature.space.domain.usecase.GetDefaultSpaceHeroUseCase
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase
import com.noztek.xend.feature.space.presentation.state.SpaceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpaceViewModel(
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getDefaultSpaceHero: GetDefaultSpaceHeroUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(SpaceUiState())
    val state: StateFlow<SpaceUiState> = _state.asStateFlow()

    init {
        refresh()
        syncFromApi()
        scope.launch {
            realtimeSignals.spaceRefreshTick.collect {
                if (it > 0) refresh()
            }
        }
    }

    fun refresh() {
        scope.launch {
            loadSpaceState(clearMessage = true)
        }
    }

    fun syncFromApi() {
        scope.launch {
            runCatching { syncRelationshipSpaces() }
                .onFailure { error ->
                    _state.update { current ->
                        if (current.defaultSpace == null && current.hero == null) {
                            current.copy(message = error.message ?: "Failed to sync spaces")
                        } else {
                            current
                        }
                    }
                }
            loadSpaceState(clearMessage = false)
        }
    }

    private suspend fun loadSpaceState(
        clearMessage: Boolean,
    ) {
        _state.update {
            it.copy(
                isLoading = true,
                message = if (clearMessage) null else it.message,
            )
        }
        runCatching { getDefaultRelationshipSpace() }
            .onSuccess { defaultSpace ->
                val hero = getDefaultSpaceHero(defaultSpace)
                _state.update {
                    it.copy(
                        isLoading = false,
                        defaultSpace = defaultSpace,
                        hero = hero,
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Failed to load spaces",
                    )
                }
            }
    }
}
