package com.noztek.xend.feature.games.presentation.state

import com.noztek.xend.feature.games.domain.model.GameCategory
import com.noztek.xend.feature.games.domain.model.GamesOverviewModel

data class GamesUiState(
    val isLoading: Boolean = true,
    val overview: GamesOverviewModel? = null,
    val selectedCategory: GameCategory = GameCategory.All,
    val message: String? = null,
)
