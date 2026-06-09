package com.noztek.xend.feature.space.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.feature.space.domain.usecase.ConfigureRelationshipSpaceAccessUseCase
import com.noztek.xend.feature.space.domain.usecase.GetHiddenRelationshipSpacesUseCase
import com.noztek.xend.feature.space.domain.usecase.SetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase
import com.noztek.xend.feature.space.domain.usecase.UnlockRelationshipSpaceUseCase
import com.noztek.xend.feature.space.presentation.state.HiddenSpacesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HiddenSpacesViewModel(
    private val getHiddenRelationshipSpaces: GetHiddenRelationshipSpacesUseCase,
    private val setDefaultRelationshipSpace: SetDefaultRelationshipSpaceUseCase,
    private val configureRelationshipSpaceAccess: ConfigureRelationshipSpaceAccessUseCase,
    private val unlockRelationshipSpace: UnlockRelationshipSpaceUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(HiddenSpacesUiState())
    val state: StateFlow<HiddenSpacesUiState> = _state.asStateFlow()

    init {
        sync()
    }

    fun sync() {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching {
                syncRelationshipSpaces()
                getHiddenRelationshipSpaces()
            }.onSuccess { items ->
                _state.update { it.copy(isLoading = false, items = items) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, message = error.message ?: "Failed to load hidden spaces") }
            }
        }
    }

    fun setDefault(spaceId: String) {
        scope.launch {
            runCatching {
                setDefaultRelationshipSpace(spaceId)
                syncRelationshipSpaces()
                getHiddenRelationshipSpaces()
            }.onSuccess { items ->
                _state.update { it.copy(items = items, message = null) }
            }.onFailure { error ->
                _state.update { it.copy(message = error.message ?: "Failed to set default space") }
            }
        }
    }

    fun saveAccess(spaceId: String, passphrase: String, hint: String?) {
        scope.launch {
            runCatching {
                configureRelationshipSpaceAccess(spaceId, passphrase, hint)
                syncRelationshipSpaces()
                getHiddenRelationshipSpaces()
            }.onSuccess { items ->
                _state.update { it.copy(items = items, message = "Hidden access updated") }
            }.onFailure { error ->
                _state.update { it.copy(message = error.message ?: "Failed to save hidden access") }
            }
        }
    }

    fun unlock(passphrase: String) {
        scope.launch {
            runCatching { unlockRelationshipSpace(passphrase) }
                .onSuccess { space ->
                    _state.update { it.copy(unlockedSpaceId = space.relationshipSpaceId, message = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(message = error.message ?: "Unable to unlock hidden space") }
                }
        }
    }

    fun clearUnlockedSpace() {
        _state.update { it.copy(unlockedSpaceId = null) }
    }
}
