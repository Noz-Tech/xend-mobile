package com.noztek.xend.feature.invites.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.feature.invites.domain.usecase.GetInboxInvitesUseCase
import com.noztek.xend.feature.invites.presentation.state.InviteBadgeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InviteBadgeViewModel(
    private val getInboxInvites: GetInboxInvitesUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(InviteBadgeUiState())
    val state: StateFlow<InviteBadgeUiState> = _state.asStateFlow()

    init {
        refresh()
        scope.launch {
            realtimeSignals.inviteRefreshTick.collect {
                if (it > 0) refresh()
            }
        }
    }

    fun refresh() {
        scope.launch {
            runCatching { getInboxInvites() }
                .onSuccess { inbox ->
                    _state.update { it.copy(pendingCount = inbox.count { invite -> invite.status == "pending" }) }
                }
        }
    }
}
