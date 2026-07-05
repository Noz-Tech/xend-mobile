package com.noztek.xend.feature.dailyritual.domain.repository

import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualOverviewModel

interface DailyRitualRepository {
    suspend fun getOverview(): DailyRitualOverviewModel
}
