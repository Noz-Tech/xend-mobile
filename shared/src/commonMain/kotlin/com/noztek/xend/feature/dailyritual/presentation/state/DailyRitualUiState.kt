package com.noztek.xend.feature.dailyritual.presentation.state

import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualOverviewModel

data class DailyRitualUiState(
    val isLoading: Boolean = true,
    val overview: DailyRitualOverviewModel? = null,
    val message: String? = null,
)
