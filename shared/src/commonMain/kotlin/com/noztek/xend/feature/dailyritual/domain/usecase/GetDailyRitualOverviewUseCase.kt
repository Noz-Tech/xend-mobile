package com.noztek.xend.feature.dailyritual.domain.usecase

import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualOverviewModel
import com.noztek.xend.feature.dailyritual.domain.repository.DailyRitualRepository

class GetDailyRitualOverviewUseCase(
    private val repository: DailyRitualRepository,
) {
    suspend operator fun invoke(): DailyRitualOverviewModel = repository.getOverview()
}
