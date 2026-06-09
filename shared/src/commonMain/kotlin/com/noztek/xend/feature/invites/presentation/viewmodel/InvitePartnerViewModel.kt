package com.noztek.xend.feature.invites.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.feature.invites.domain.usecase.GetSentInvitesUseCase
import com.noztek.xend.feature.invites.domain.usecase.RelationshipInviteSubmissionUseCase
import com.noztek.xend.feature.invites.presentation.state.InviteUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvitePartnerViewModel(
    private val submitRelationshipInvite: RelationshipInviteSubmissionUseCase,
    private val getSentInvites: GetSentInvitesUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(InviteUiState())
    val state: StateFlow<InviteUiState> = _state.asStateFlow()

    init {
        refreshSentInvites()
    }

    fun onIdentifierChanged(value: String) {
        _state.update { it.copy(identifier = value, message = null) }
    }

    fun onNoteChanged(value: String) {
        _state.update { it.copy(note = value, message = null) }
    }

    fun submit() {
        val current = _state.value
        if (current.identifier.isBlank()) {
            _state.update { it.copy(message = "Identifier is required") }
            return
        }

        scope.launch {
            _state.update { it.copy(isLoading = true, message = null, inviteId = null) }
            runCatching {
                submitRelationshipInvite(current.identifier, current.note)
            }.onSuccess { result ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        inviteId = result.inviteId,
                        message = "Invite sent",
                        sentInvites = result.sentInvites,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Failed to send invite",
                    )
                }
            }
        }
    }

    private fun refreshSentInvites() {
        scope.launch {
            runCatching { getSentInvites() }
                .onSuccess { items -> _state.update { it.copy(sentInvites = items) } }
        }
    }
}
