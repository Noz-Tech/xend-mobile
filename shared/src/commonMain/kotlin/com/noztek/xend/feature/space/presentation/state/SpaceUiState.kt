package com.noztek.xend.feature.space.presentation.state

import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.model.SpaceHeroModel

data class SpaceUiState(
    val isLoading: Boolean = true,
    val defaultSpace: RelationshipSpaceCardModel? = null,
    val hero: SpaceHeroModel? = null,
    val message: String? = null,
)
