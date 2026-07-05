package com.noztek.xend.feature.games.domain.repository

import com.noztek.xend.feature.games.domain.model.GamesOverviewModel

interface GamesRepository {
    suspend fun getOverview(): GamesOverviewModel
}
