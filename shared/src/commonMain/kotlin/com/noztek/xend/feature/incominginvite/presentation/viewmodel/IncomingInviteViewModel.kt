package com.noztek.xend.feature.incominginvite.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.feature.incominginvite.domain.usecase.AcceptIncomingInviteUseCase
import com.noztek.xend.feature.incominginvite.domain.usecase.DeclineIncomingInviteUseCase
import com.noztek.xend.feature.incominginvite.domain.usecase.LoadIncomingInviteUseCase
import com.noztek.xend.feature.incominginvite.presentation.state.IncomingInviteUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IncomingInviteViewModel(
    private val loadIncomingInvite: LoadIncomingInviteUseCase,
    private val acceptIncomingInvite: AcceptIncomingInviteUseCase,
    private val declineIncomingInvite: DeclineIncomingInviteUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(IncomingInviteUiState())
    val state: StateFlow<IncomingInviteUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh(showLoading: Boolean = true) {
        scope.launch {
            if (showLoading) {
                _state.update { it.copy(isLoading = true, message = null) }
            } else {
                _state.update { it.copy(message = null) }
            }
            runCatching { loadIncomingInvite() }
                .onSuccess { invite ->
                    _state.update {
                        when {
                            invite == null -> it.copy(
                                isLoading = false,
                                invite = null,
                                shouldReturnToSpaceSetup = true,
                            )
                            invite.hasRelationshipSpace -> it.copy(
                                isLoading = false,
                                invite = invite,
                                shouldEnterMain = true,
                            )
                            else -> it.copy(
                                isLoading = false,
                                invite = invite,
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
                            message = error.message ?: "Failed to load your invite.",
                        )
                    }
                }
        }
    }

    fun acceptInvite() {
        val inviteId = _state.value.invite?.inviteId ?: return
        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { acceptIncomingInvite(inviteId) }
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false, shouldEnterMain = true) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = error.message ?: "Failed to accept invite.",
                        )
                    }
                }
        }
    }

    fun declineInvite() {
        val inviteId = _state.value.invite?.inviteId ?: return
        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { declineIncomingInvite(inviteId) }
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
                            message = error.message ?: "Failed to decline invite.",
                        )
                    }
                }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun consumeEnterMain() {
        _state.update { it.copy(shouldEnterMain = false) }
    }

    fun consumeReturnToSpaceSetup() {
        _state.update { it.copy(shouldReturnToSpaceSetup = false) }
    }
}
