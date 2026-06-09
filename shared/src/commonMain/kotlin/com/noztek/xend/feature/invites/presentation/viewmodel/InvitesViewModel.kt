package com.noztek.xend.feature.invites.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.feature.invites.domain.usecase.DeclineRelationshipInviteUseCase
import com.noztek.xend.feature.invites.domain.usecase.GetInboxInvitesUseCase
import com.noztek.xend.feature.invites.domain.usecase.GetSentInvitesUseCase
import com.noztek.xend.feature.invites.domain.usecase.RelationshipInviteAcceptanceCompleter
import com.noztek.xend.feature.invites.presentation.state.InvitesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvitesViewModel(
    private val getInboxInvites: GetInboxInvitesUseCase,
    private val getSentInvites: GetSentInvitesUseCase,
    private val completeInviteAcceptance: RelationshipInviteAcceptanceCompleter,
    private val declineRelationshipInvite: DeclineRelationshipInviteUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(InvitesUiState())
    val state: StateFlow<InvitesUiState> = _state.asStateFlow()

    init {
        refresh(showLoading = true)
        scope.launch {
            realtimeSignals.inviteRefreshTick.collect {
                if (it > 0) refresh()
            }
        }
    }

    fun refresh(showLoading: Boolean = false) {
        scope.launch {
            val hasData = _state.value.inboxInvites.isNotEmpty() || _state.value.sentInvites.isNotEmpty()
            if (showLoading || !hasData) {
                _state.update { it.copy(isLoading = true, message = null) }
            } else {
                _state.update { it.copy(message = null) }
            }
            runCatching {
                val inbox = getInboxInvites()
                val sent = getSentInvites()
                inbox to sent
            }.onSuccess { (inbox, sent) ->
                _state.update { it.copy(isLoading = false, inboxInvites = inbox, sentInvites = sent) }
            }.onFailure { err ->
                _state.update { it.copy(isLoading = false, message = err.message ?: "Failed to load invites") }
            }
        }
    }

    fun acceptInvite(inviteId: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching {
                completeInviteAcceptance.accept(inviteId)
            }.onSuccess {
                refresh()
            }.onFailure { err ->
                _state.update { it.copy(isLoading = false, message = err.message ?: "Failed to accept invite") }
            }
        }
    }

    fun declineInvite(inviteId: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { declineRelationshipInvite(inviteId) }
                .onSuccess { refresh() }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, message = err.message ?: "Failed to decline invite") }
                }
        }
    }
}
