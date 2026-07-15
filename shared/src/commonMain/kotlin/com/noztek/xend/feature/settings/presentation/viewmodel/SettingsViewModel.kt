package com.noztek.xend.feature.settings.presentation.viewmodel

import com.noztek.xend.feature.auth.domain.usecase.ClearPendingAuthFlowUseCase
import com.noztek.xend.feature.auth.domain.usecase.CompleteLogoutSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.noztek.xend.feature.auth.domain.usecase.LogoutUseCase
import com.noztek.xend.feature.settings.presentation.state.SettingsUiState
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.GetDefaultSpaceHeroUseCase
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

class SettingsViewModel(
    private val getCurrentUserProfile: GetCurrentUserProfileUseCase,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getDefaultSpaceHero: GetDefaultSpaceHeroUseCase,
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
            val profile = getCurrentUserProfile()
            val defaultSpace = getDefaultRelationshipSpace()
            val hero = getDefaultSpaceHero(defaultSpace)
            val coupleTitle = hero?.let { "${it.userName} & ${it.partnerName}" }
                ?: defaultSpace?.name?.takeIf { it.isNotBlank() }
                ?: "Couple Space"
            val coupleSubtitle = defaultSpace?.createdAtEpochSeconds
                ?.takeIf { it > 0L }
                ?.let { "Together since ${formatJoinedDate(it)}" }
                ?: "Manage your shared space settings."

            _state.update {
                it.copy(
                    profile = profile,
                    coupleSpaceTitle = coupleTitle,
                    coupleSpaceSubtitle = coupleSubtitle,
                )
            }
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

    private fun formatJoinedDate(epochSeconds: Long): String {
        val date = Instant.fromEpochSeconds(epochSeconds)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        return "${date.monthDisplayName()} ${date.day}, ${date.year}"
    }

    private fun LocalDate.monthDisplayName(): String = when (month) {
        Month.JANUARY -> "January"
        Month.FEBRUARY -> "February"
        Month.MARCH -> "March"
        Month.APRIL -> "April"
        Month.MAY -> "May"
        Month.JUNE -> "June"
        Month.JULY -> "July"
        Month.AUGUST -> "August"
        Month.SEPTEMBER -> "September"
        Month.OCTOBER -> "October"
        Month.NOVEMBER -> "November"
        Month.DECEMBER -> "December"
    }
}
