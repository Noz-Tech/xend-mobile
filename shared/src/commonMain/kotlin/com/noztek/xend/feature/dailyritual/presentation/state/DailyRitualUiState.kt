package com.noztek.xend.feature.dailyritual.presentation.state

import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualOverviewModel

data class DailyRitualUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val overview: DailyRitualOverviewModel? = null,
    val responseDraft: String = "",
    val isResponseComposerVisible: Boolean = false,
    val message: String? = null,
)
