package com.noztek.xend.feature.space.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.core.ui.media.RemoteImageMemoryCache
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualTodayModel
import com.noztek.xend.feature.dailyritual.domain.usecase.GetDailyRitualOverviewUseCase
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.usecase.GetCurrentSpaceMoodsUseCase
import com.noztek.xend.feature.space.domain.usecase.GetDefaultSpaceHeroUseCase
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.GetRelationshipSpaceMediaImageUseCase
import com.noztek.xend.feature.space.domain.usecase.SetSpaceMoodUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase
import com.noztek.xend.feature.space.presentation.state.SpaceTodayRitualModel
import com.noztek.xend.feature.space.presentation.state.SpaceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpaceViewModel(
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getDefaultSpaceHero: GetDefaultSpaceHeroUseCase,
    private val getCurrentSpaceMoods: GetCurrentSpaceMoodsUseCase,
    private val setSpaceMood: SetSpaceMoodUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val getDailyRitualOverview: GetDailyRitualOverviewUseCase,
    private val getSpaceMediaImage: GetRelationshipSpaceMediaImageUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(SpaceUiState())
    val state: StateFlow<SpaceUiState> = _state.asStateFlow()
    private var loadedCouplePhotoKey: String? = null
    private var requestedCouplePhotoKey: String? = null

    init {
        refresh()
        syncFromApi()
        scope.launch {
            realtimeSignals.spaceRefreshTick.collect {
                if (it > 0) refresh()
            }
        }
        scope.launch {
            realtimeSignals.moodRefreshTick.collect {
                if (it > 0) refreshMoods()
            }
        }
    }

    fun refresh() {
        scope.launch {
            loadSpaceState(clearMessage = true)
        }
    }

    fun syncFromApi() {
        scope.launch {
            runCatching { syncRelationshipSpaces() }
                .onFailure { error ->
                    _state.update { current ->
                        if (current.defaultSpace == null && current.hero == null) {
                            current.copy(message = error.message ?: "Failed to sync spaces")
                        } else {
                            current
                        }
                    }
                }
            loadSpaceState(clearMessage = false)
        }
    }

    fun setMood(moodKey: String, emoji: String, label: String) {
        val spaceID = _state.value.defaultSpace?.relationshipSpaceId ?: return
        if (_state.value.isSavingMood) return

        scope.launch {
            _state.update { it.copy(isSavingMood = true, message = null) }
            runCatching { setSpaceMood(spaceID, moodKey, emoji, label) }
                .onSuccess { moods ->
                    _state.update {
                        it.copy(
                            moods = moods,
                            isSavingMood = false,
                            message = null,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSavingMood = false,
                            message = error.message ?: "Failed to update mood",
                        )
                    }
                }
        }
    }

    private fun refreshMoods() {
        val spaceID = _state.value.defaultSpace?.relationshipSpaceId ?: return
        scope.launch {
            runCatching { getCurrentSpaceMoods(spaceID) }
                .onSuccess { moods ->
                    _state.update { it.copy(moods = moods) }
                }
        }
    }

    private suspend fun loadSpaceState(
        clearMessage: Boolean,
    ) {
        _state.update {
            it.copy(
                isLoading = true,
                message = if (clearMessage) null else it.message,
            )
        }
        runCatching { getDefaultRelationshipSpace() }
            .onSuccess { defaultSpace ->
                val hero = getDefaultSpaceHero(defaultSpace)
                val moods = defaultSpace
                    ?.let { space -> runCatching { getCurrentSpaceMoods(space.relationshipSpaceId) }.getOrDefault(emptyList()) }
                    .orEmpty()
                val todayRitual = defaultSpace
                    ?.let { runCatching { getDailyRitualOverview().todayRitual?.toSpaceModel() }.getOrNull() }
                _state.update {
                    it.copy(
                        isLoading = false,
                        defaultSpace = defaultSpace,
                        hero = hero,
                        moods = moods,
                        todayRitual = todayRitual,
                    )
                }
                loadCouplePhoto(defaultSpace)
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Failed to load spaces",
                    )
                }
            }
    }

    private fun DailyRitualTodayModel.toSpaceModel(): SpaceTodayRitualModel {
        return SpaceTodayRitualModel(
            title = title,
            description = description,
            rewardPoints = rewardPoints,
            completed = completed,
        )
    }

    private fun loadCouplePhoto(space: RelationshipSpaceCardModel?) {
        if (space?.couplePhotoUrl.isNullOrBlank()) {
            loadedCouplePhotoKey = null
            requestedCouplePhotoKey = null
            _state.update { it.copy(couplePhoto = null) }
            return
        }

        val resolvedSpace = requireNotNull(space)
        val cacheKey = mediaCacheKey(resolvedSpace, COUPLE_PHOTO_KIND)
        val currentState = _state.value
        if (loadedCouplePhotoKey == cacheKey && currentState.couplePhoto != null) return

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

    private fun mediaCacheKey(space: RelationshipSpaceCardModel, kind: String): String {
        return "${space.relationshipSpaceId}:$kind:${space.updatedAtEpochSeconds}"
    }

    private companion object {
        const val COUPLE_PHOTO_KIND = "couple-photo"
    }
}
