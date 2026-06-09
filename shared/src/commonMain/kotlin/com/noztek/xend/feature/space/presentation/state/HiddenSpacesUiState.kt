package com.noztek.xend.feature.space.presentation.state

import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel

data class HiddenSpacesUiState(
    val isLoading: Boolean = true,
    val items: List<RelationshipSpaceCardModel> = emptyList(),
    val message: String? = null,
    val unlockedSpaceId: String? = null,
)
