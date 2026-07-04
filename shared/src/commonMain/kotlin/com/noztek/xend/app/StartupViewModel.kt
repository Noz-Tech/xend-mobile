package com.noztek.xend.app

import com.noztek.xend.core.network.ApiHealthChecker
import com.noztek.xend.feature.auth.domain.model.PendingAuthFlowStep
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.GetPendingAuthFlowUseCase
import com.noztek.xend.feature.device.domain.usecase.EnsureLocalSignalBootstrapUseCase
import com.noztek.xend.feature.spacesetup.domain.model.AuthenticatedEntryDestination
import com.noztek.xend.feature.spacesetup.domain.usecase.ResolveAuthenticatedEntryDestinationUseCase
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
    private val getPendingAuthFlow: GetPendingAuthFlowUseCase,
    private val resolveAuthenticatedEntryDestination: ResolveAuthenticatedEntryDestinationUseCase,
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
            _state.update { it.copy(isChecking = true, isApiOnline = null, destination = null) }
            val session = getCurrentSession()
            val pendingFlow = if (session == null) getPendingAuthFlow() else null
            val hasSession = session != null
            val online = healthChecker.isOnline()
            val authenticatedEntryDestination = if (hasSession && online) {
                resolveAuthenticatedEntryDestination()
            } else {
                null
            }
            val destination = when {
                !online -> StartupDestination.OFFLINE
                authenticatedEntryDestination == AuthenticatedEntryDestination.MAIN -> StartupDestination.MAIN
                authenticatedEntryDestination == AuthenticatedEntryDestination.INCOMING_INVITE -> StartupDestination.INCOMING_INVITE
                authenticatedEntryDestination == AuthenticatedEntryDestination.OUTGOING_INVITE -> StartupDestination.OUTGOING_INVITE
                authenticatedEntryDestination == AuthenticatedEntryDestination.SPACE_SETUP -> StartupDestination.SPACE_SETUP
                pendingFlow?.step == PendingAuthFlowStep.VERIFY_EMAIL && pendingFlow.email.isNotBlank() -> StartupDestination.VERIFY_EMAIL
                else -> StartupDestination.WELCOME
            }
            _state.update {
                it.copy(
                    isChecking = false,
                    isApiOnline = online,
                    hasSession = hasSession,
                    destination = destination,
                    pendingVerificationEmail = pendingFlow?.email,
                    pendingVerificationResendAvailableAtEpochSeconds =
                        pendingFlow?.resendAvailableAtEpochSeconds ?: 0,
                )
            }
        }
    }
}
