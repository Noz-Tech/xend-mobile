package com.noztek.xend.feature.settings.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.core.ui.media.RemoteImageMemoryCache
import com.noztek.xend.core.ui.media.decodeRemoteImageBitmap
import com.noztek.xend.feature.settings.presentation.state.CoupleSettingsUiState
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.GetRelationshipSpaceMediaImageUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase
import com.noztek.xend.feature.space.domain.usecase.UpdateRelationshipSpaceSettingsUseCase
import com.noztek.xend.feature.space.domain.usecase.UploadRelationshipSpaceCouplePhotoUseCase
import com.noztek.xend.feature.space.domain.usecase.UploadRelationshipSpaceCoverPhotoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoupleSettingsViewModel(
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val updateRelationshipSpaceSettings: UpdateRelationshipSpaceSettingsUseCase,
    private val uploadCoverPhotoUseCase: UploadRelationshipSpaceCoverPhotoUseCase,
    private val uploadCouplePhotoUseCase: UploadRelationshipSpaceCouplePhotoUseCase,
    private val getSpaceMediaImage: GetRelationshipSpaceMediaImageUseCase,
    private val realtimeSignals: RealtimeFeatureSignals,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(CoupleSettingsUiState())
    val state: StateFlow<CoupleSettingsUiState> = _state.asStateFlow()
    private var loadedCoverPhotoKey: String? = null
    private var loadedCouplePhotoKey: String? = null

    init {
        refresh()
        scope.launch {
            realtimeSignals.spaceRefreshTick.collect { tick ->
                if (tick > 0L) refreshFromLocal()
            }
        }
    }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching {
                syncRelationshipSpaces()
                requireNotNull(getDefaultRelationshipSpace()) { "No active space yet" }
            }.onSuccess { space ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        space = space,
                        message = null,
                    )
                }
                loadMedia(space)
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Failed to load couple settings.",
                    )
                }
            }
        }
    }

    fun saveName(name: String) {
        val space = _state.value.space ?: return
        if (_state.value.isSavingName) return

        scope.launch {
            _state.update { it.copy(isSavingName = true, message = null) }
            runCatching {
                updateRelationshipSpaceSettings(
                    spaceId = space.relationshipSpaceId,
                    name = name.trim().takeIf { it.isNotBlank() },
                )
            }.onSuccess { updated ->
                _state.update {
                    it.copy(
                        isSavingName = false,
                        space = updated,
                        message = null,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSavingName = false,
                        message = error.message ?: "Failed to update couple name.",
                    )
                }
            }
        }
    }

    fun saveRelationshipStartDate(date: String) {
        val space = _state.value.space ?: return
        if (_state.value.isSavingRelationshipStartDate) return

        scope.launch {
            _state.update { it.copy(isSavingRelationshipStartDate = true, message = null) }
            runCatching {
                updateRelationshipSpaceSettings(
                    spaceId = space.relationshipSpaceId,
                    name = space.name.trim().takeIf { it.isNotBlank() },
                    relationshipStartDate = date.trim(),
                )
            }.onSuccess { updated ->
                _state.update {
                    it.copy(
                        isSavingRelationshipStartDate = false,
                        space = updated,
                        message = null,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSavingRelationshipStartDate = false,
                        message = error.message ?: "Failed to update relationship start date.",
                    )
                }
            }
        }
    }

    fun uploadCover(image: PickedImageData) {
        val space = _state.value.space ?: return
        if (_state.value.isUploadingCoverPhoto) return

        scope.launch {
            _state.update { it.copy(isUploadingCoverPhoto = true, message = null) }
            runCatching { uploadCoverPhotoUseCase(space.relationshipSpaceId, image) }
                .onSuccess { updated ->
                    val preview = decodeRemoteImageBitmap(image.bytes)
                    val cacheKey = mediaCacheKey(updated, COVER_PHOTO_KIND)
                    if (preview != null) {
                        RemoteImageMemoryCache.put(cacheKey, preview)
                        loadedCoverPhotoKey = cacheKey
                    }
                    _state.update {
                        it.copy(
                            isUploadingCoverPhoto = false,
                            space = updated,
                            coverPhoto = preview ?: it.coverPhoto,
                            message = null,
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isUploadingCoverPhoto = false,
                            message = error.message ?: "Failed to upload cover photo.",
                        )
                    }
                }
        }
    }

    fun uploadCouplePhoto(image: PickedImageData) {
        val space = _state.value.space ?: return
        if (_state.value.isUploadingCouplePhoto) return

        scope.launch {
            _state.update { it.copy(isUploadingCouplePhoto = true, message = null) }
            runCatching { uploadCouplePhotoUseCase(space.relationshipSpaceId, image) }
                .onSuccess { updated ->
                    val preview = decodeRemoteImageBitmap(image.bytes)
                    val cacheKey = mediaCacheKey(updated, COUPLE_PHOTO_KIND)
                    if (preview != null) {
                        RemoteImageMemoryCache.put(cacheKey, preview)
                        loadedCouplePhotoKey = cacheKey
                    }
                    _state.update {
                        it.copy(
                            isUploadingCouplePhoto = false,
                            space = updated,
                            couplePhoto = preview ?: it.couplePhoto,
                            message = null,
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isUploadingCouplePhoto = false,
                            message = error.message ?: "Failed to upload couple photo.",
                        )
                    }
                }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun refreshFromLocal() {
        scope.launch {
            val space = getDefaultRelationshipSpace() ?: return@launch
            _state.update { it.copy(space = space) }
            loadMedia(space)
        }
    }

    private fun loadMedia(space: RelationshipSpaceCardModel) {
        loadCoverPhoto(space)
        loadCouplePhoto(space)
    }

    private fun loadCoverPhoto(space: RelationshipSpaceCardModel) {
        if (space.coverPhotoUrl.isNullOrBlank()) {
            loadedCoverPhotoKey = null
            _state.update { it.copy(coverPhoto = null) }
            return
        }

        val cacheKey = mediaCacheKey(space, COVER_PHOTO_KIND)
        val currentState = _state.value
        if (loadedCoverPhotoKey == cacheKey && currentState.coverPhoto != null) return

        RemoteImageMemoryCache.get(cacheKey)?.let { cached ->
            loadedCoverPhotoKey = cacheKey
            _state.update { it.copy(coverPhoto = cached) }
            return
        }

        scope.launch {
            runCatching { getSpaceMediaImage(space.relationshipSpaceId, COVER_PHOTO_KIND) }
                .onSuccess { image ->
                    RemoteImageMemoryCache.put(cacheKey, image)
                    loadedCoverPhotoKey = cacheKey
                    _state.update { it.copy(coverPhoto = image) }
                }
        }
    }

    private fun loadCouplePhoto(space: RelationshipSpaceCardModel) {
        if (space.couplePhotoUrl.isNullOrBlank()) {
            loadedCouplePhotoKey = null
            _state.update { it.copy(couplePhoto = null) }
            return
        }

        val cacheKey = mediaCacheKey(space, COUPLE_PHOTO_KIND)
        val currentState = _state.value
        if (loadedCouplePhotoKey == cacheKey && currentState.couplePhoto != null) return

        RemoteImageMemoryCache.get(cacheKey)?.let { cached ->
            loadedCouplePhotoKey = cacheKey
            _state.update { it.copy(couplePhoto = cached) }
            return
        }

        scope.launch {
            runCatching { getSpaceMediaImage(space.relationshipSpaceId, COUPLE_PHOTO_KIND) }
                .onSuccess { image ->
                    RemoteImageMemoryCache.put(cacheKey, image)
                    loadedCouplePhotoKey = cacheKey
                    _state.update { it.copy(couplePhoto = image) }
                }
        }
    }

    private fun mediaCacheKey(space: RelationshipSpaceCardModel, kind: String): String {
        return "${space.relationshipSpaceId}:$kind:${space.updatedAtEpochSeconds}"
    }

    private companion object {
        const val COVER_PHOTO_KIND = "cover-photo"
        const val COUPLE_PHOTO_KIND = "couple-photo"
    }
}
