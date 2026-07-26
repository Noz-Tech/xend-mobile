package com.noztek.xend.feature.settings.presentation.viewmodel

import com.noztek.xend.feature.auth.domain.usecase.ClearPendingAuthFlowUseCase
import com.noztek.xend.feature.auth.domain.usecase.CompleteLogoutSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.noztek.xend.feature.auth.domain.usecase.LogoutUseCase
import com.noztek.xend.core.ui.media.RemoteImageMemoryCache
import com.noztek.xend.feature.settings.presentation.state.SettingsUiState
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.GetDefaultSpaceHeroUseCase
import com.noztek.xend.feature.space.domain.usecase.GetRelationshipSpaceMediaImageUseCase
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
    private val getSpaceMediaImage: GetRelationshipSpaceMediaImageUseCase,
    private val logout: LogoutUseCase,
    private val clearPendingAuthFlow: ClearPendingAuthFlowUseCase,
    private val completeLogoutSession: CompleteLogoutSessionUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()
    private var loadedCouplePhotoKey: String? = null
    private var requestedCouplePhotoKey: String? = null

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
            val coupleSubtitle = defaultSpace?.relationshipStartDate
                ?.takeIf { it.isNotBlank() }
                ?.let { "Together since ${formatJoinedDate(it)}" }
                ?: defaultSpace?.createdAtEpochSeconds
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
            loadCouplePhoto(defaultSpace)
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

    private fun formatJoinedDate(isoDate: String): String {
        val date = runCatching { LocalDate.parse(isoDate) }.getOrNull()
            ?: return isoDate
        return "${date.monthDisplayName()} ${date.day}, ${date.year}"
    }

    private fun loadCouplePhoto(space: RelationshipSpaceCardModel?) {
        if (space?.couplePhotoUrl.isNullOrBlank()) {
            loadedCouplePhotoKey = null
            requestedCouplePhotoKey = null
            _state.update { it.copy(couplePhoto = null) }
            return
        }

        val resolvedSpace = requireNotNull(space)
        val cacheKey = mediaCacheKey(resolvedSpace)
        if (loadedCouplePhotoKey == cacheKey && _state.value.couplePhoto != null) return

        RemoteImageMemoryCache.get(cacheKey)?.let { cached ->
            loadedCouplePhotoKey = cacheKey
            requestedCouplePhotoKey = null
            _state.update { it.copy(couplePhoto = cached) }
            return
        }

        if (requestedCouplePhotoKey == cacheKey) return
        requestedCouplePhotoKey = cacheKey

        scope.launch {
            runCatching { getSpaceMediaImage(resolvedSpace.relationshipSpaceId, COUPLE_PHOTO_KIND) }
                .onSuccess { image ->
                    RemoteImageMemoryCache.put(cacheKey, image)
                    loadedCouplePhotoKey = cacheKey
                    requestedCouplePhotoKey = null
                    _state.update { it.copy(couplePhoto = image) }
                }
                .onFailure {
                    if (requestedCouplePhotoKey == cacheKey) {
                        requestedCouplePhotoKey = null
                    }
                }
        }
    }

    private fun mediaCacheKey(space: RelationshipSpaceCardModel): String {
        val version = space.couplePhotoVersion
            ?.takeIf { it.isNotBlank() }
            ?: space.updatedAtEpochSeconds.toString()
        return "${space.relationshipSpaceId}:$COUPLE_PHOTO_KIND:$version"
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

    private companion object {
        const val COUPLE_PHOTO_KIND = "couple-photo"
    }
}
