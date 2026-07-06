package com.noztek.xend.feature.settings.presentation.viewmodel

import com.noztek.xend.feature.auth.domain.usecase.ClearPendingAuthFlowUseCase
import com.noztek.xend.feature.auth.domain.usecase.CompleteLogoutSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.noztek.xend.feature.auth.domain.usecase.LogoutUseCase
import com.noztek.xend.feature.settings.presentation.state.SettingsUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getCurrentUserProfile: GetCurrentUserProfileUseCase,
    private val logout: LogoutUseCase,
    private val clearPendingAuthFlow: ClearPendingAuthFlowUseCase,
    private val completeLogoutSession: CompleteLogoutSessionUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(profile = getCurrentUserProfile()) }
        }
    }

    fun logout() {
        if (_state.value.isLoading) return

        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching {
                logout()
                clearPendingAuthFlow()
                completeLogoutSession()
            }.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        profile = null,
                        message = null,
                        isLoggedOut = true,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Logout failed",
                    )
                }
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
