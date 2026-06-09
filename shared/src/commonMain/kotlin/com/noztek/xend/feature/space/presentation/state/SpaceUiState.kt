package com.noztek.xend.feature.space.presentation.state

import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel

data class SpaceUiState(
    val isLoading: Boolean = true,
    val defaultSpace: RelationshipSpaceCardModel? = null,
    val unreadCount: Int = 0,
    val message: String? = null,
)
