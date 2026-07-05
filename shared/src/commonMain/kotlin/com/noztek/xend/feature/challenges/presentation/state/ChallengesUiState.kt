package com.noztek.xend.feature.challenges.presentation.state

import com.noztek.xend.feature.challenges.domain.model.ChallengeAudience
import com.noztek.xend.feature.challenges.domain.model.ChallengeCategory
import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel

data class ChallengesUiState(
    val isLoading: Boolean = true,
    val overview: ChallengesOverviewModel? = null,
    val selectedAudience: ChallengeAudience = ChallengeAudience.ForYou,
    val selectedCategory: ChallengeCategory = ChallengeCategory.All,
    val message: String? = null,
)
