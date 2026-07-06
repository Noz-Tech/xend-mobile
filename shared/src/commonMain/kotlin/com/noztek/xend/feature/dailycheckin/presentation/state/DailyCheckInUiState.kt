package com.noztek.xend.feature.dailycheckin.presentation.state

import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInOverviewModel

data class DailyCheckInUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val overview: DailyCheckInOverviewModel? = null,
    val message: String? = null,
)
