package com.noztek.xend.feature.settings.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel

data class CoupleSettingsUiState(
    val isLoading: Boolean = false,
    val isSavingName: Boolean = false,
    val isUploadingCoverPhoto: Boolean = false,
    val isUploadingCouplePhoto: Boolean = false,
    val space: RelationshipSpaceCardModel? = null,
    val coverPhoto: ImageBitmap? = null,
    val couplePhoto: ImageBitmap? = null,
    val message: String? = null,
)
