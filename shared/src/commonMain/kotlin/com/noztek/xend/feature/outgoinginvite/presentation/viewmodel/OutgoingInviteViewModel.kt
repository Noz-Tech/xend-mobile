package com.noztek.xend.feature.outgoinginvite.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.feature.invites.domain.usecase.DeclineRelationshipInviteUseCase
import com.noztek.xend.feature.outgoinginvite.domain.usecase.LoadOutgoingInviteUseCase
import com.noztek.xend.feature.outgoinginvite.presentation.state.OutgoingInviteUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OutgoingInviteViewModel(
    private val loadOutgoingInvite: LoadOutgoingInviteUseCase,
    private val declineRelationshipInvite: DeclineRelationshipInviteUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(OutgoingInviteUiState())
    val state: StateFlow<OutgoingInviteUiState> = _state.asStateFlow()

    init {
        refresh(showLoading = true)
        scope.launch {
            realtimeSignals.inviteRefreshTick.collect { tick ->
                if (tick > 0) refresh(showLoading = false)
            }
        }
        scope.launch {
            realtimeSignals.spaceRefreshTick.collect { tick ->
                if (tick > 0) refresh(showLoading = false)
            }
        }
    }

    fun refresh(showLoading: Boolean = true) {
        scope.launch {
            if (showLoading) {
                _state.update { it.copy(isLoading = true, message = null) }
            } else {
                _state.update { it.copy(message = null) }
            }
            runCatching { loadOutgoingInvite() }
                .onSuccess { snapshot ->
                    _state.update {
                        when {
                            snapshot.hasRelationshipSpace -> it.copy(
                                isLoading = false,
                                isSubmitting = false,
                                shouldEnterMain = true,
                                invite = snapshot.invite ?: it.invite,
                            )
                            snapshot.invite == null -> it.copy(
                                isLoading = false,
                                isSubmitting = false,
                                invite = null,
                                shouldReturnToSpaceSetup = true,
                            )
                            else -> it.copy(
                                isLoading = false,
                                isSubmitting = false,
                                invite = snapshot.invite,
                                shouldEnterMain = false,
                                shouldReturnToSpaceSetup = false,
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = error.message ?: "Failed to load your sent invite.",
                        )
                    }
                }
        }
    }

    fun cancelInvite() {
        val inviteId = _state.value.invite?.inviteId ?: return
        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { declineRelationshipInvite(inviteId) }
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            shouldReturnToSpaceSetup = true,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = error.message ?: "Failed to cancel invite.",
                        )
                    }
                }
        }
    }

    fun consumeEnterMain() {
        _state.update { it.copy(shouldEnterMain = false) }
    }

    fun consumeReturnToSpaceSetup() {
        _state.update { it.copy(shouldReturnToSpaceSetup = false) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
