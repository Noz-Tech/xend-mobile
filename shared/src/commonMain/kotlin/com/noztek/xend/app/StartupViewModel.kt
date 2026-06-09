package com.noztek.xend.app

import com.noztek.xend.core.network.ApiHealthChecker
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentSessionUseCase
import com.noztek.xend.feature.device.domain.usecase.EnsureLocalSignalBootstrapUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StartupViewModel(
    private val healthChecker: ApiHealthChecker,
    private val ensureLocalSignalBootstrap: EnsureLocalSignalBootstrapUseCase,
    private val getCurrentSession: GetCurrentSessionUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(StartupUiState())
    val state: StateFlow<StartupUiState> = _state.asStateFlow()

    init {
        ensureLocalSignalBootstrap()
        checkApiHealth()
    }

    fun checkApiHealth() {
        scope.launch {
            _state.update { it.copy(isChecking = true, isApiOnline = null) }
            val hasSession = getCurrentSession() != null
            val online = healthChecker.isOnline()
            _state.update { it.copy(isChecking = false, isApiOnline = online, hasSession = hasSession) }
        }
    }
}
