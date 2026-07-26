package com.noztek.xend.feature.space.presentation.state

import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.model.SpaceHeroModel
import com.noztek.xend.feature.space.domain.model.SpaceMoodModel

data class SpaceUiState(
    val isLoading: Boolean = true,
    val defaultSpace: RelationshipSpaceCardModel? = null,
    val hero: SpaceHeroModel? = null,
    val moods: List<SpaceMoodModel> = emptyList(),
    val todayRitual: SpaceTodayRitualModel? = null,
    val isSavingMood: Boolean = false,
    val message: String? = null,
)

data class SpaceTodayRitualModel(
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val completed: Boolean,
)
