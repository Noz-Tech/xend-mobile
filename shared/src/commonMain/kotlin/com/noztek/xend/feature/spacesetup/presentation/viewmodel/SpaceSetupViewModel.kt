package com.noztek.xend.feature.spacesetup.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.feature.spacesetup.domain.usecase.LoadSpaceSetupUseCase
import com.noztek.xend.feature.spacesetup.domain.usecase.SubmitPartnerInviteCodeUseCase
import com.noztek.xend.feature.spacesetup.presentation.state.SpaceSetupUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpaceSetupViewModel(
    private val loadSpaceSetup: LoadSpaceSetupUseCase,
    private val submitPartnerInviteCode: SubmitPartnerInviteCodeUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(SpaceSetupUiState())
    val state: StateFlow<SpaceSetupUiState> = _state.asStateFlow()

    init {
        refresh()
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
            runCatching { loadSpaceSetup() }
                .onSuccess { snapshot ->
                    _state.update {
                        val shouldOpenIncomingInvite =
                            !snapshot.hasRelationshipSpace && snapshot.pendingIncomingInvites > 0
                        val shouldOpenOutgoingInvite =
                            !snapshot.hasRelationshipSpace &&
                                snapshot.pendingIncomingInvites == 0 &&
                                snapshot.pendingSentInvites > 0
                        it.copy(
                            isLoading = false,
                            ownIdentifier = snapshot.ownIdentifier,
                            displayName = snapshot.displayName,
                            pendingIncomingInvites = snapshot.pendingIncomingInvites,
                            pendingSentInvites = snapshot.pendingSentInvites,
                            shouldOpenIncomingInvite = shouldOpenIncomingInvite,
                            shouldOpenOutgoingInvite = shouldOpenOutgoingInvite,
                            shouldEnterMain = snapshot.hasRelationshipSpace,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, message = error.message ?: "Failed to load your space setup.") }
                }
        }
    }

    fun onPartnerCodeChanged(value: String) {
        _state.update {
            it.copy(
                partnerCode = value.uppercase(),
                message = null,
            )
        }
    }

    fun submitPartnerCode() {
        val identifier = _state.value.partnerCode.trim()
        if (identifier.isBlank()) {
            _state.update { it.copy(message = "Partner invite code is required.") }
            return
        }

        scope.launch {
            _state.update { it.copy(isSubmitting = true, message = null) }
            runCatching { submitPartnerInviteCode(identifier) }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            partnerCode = "",
                            pendingSentInvites = result.sentInvites.count { invite ->
                                invite.status.equals("pending", ignoreCase = true)
                            },
                            shouldOpenOutgoingInvite = true,
                            message = null,
                        )
                    }
                    refresh()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = error.message ?: "Failed to send invite.",
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

    fun consumeOpenIncomingInvite() {
        _state.update { it.copy(shouldOpenIncomingInvite = false) }
    }

    fun consumeOpenOutgoingInvite() {
        _state.update { it.copy(shouldOpenOutgoingInvite = false) }
    }
}
