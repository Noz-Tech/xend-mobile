package com.noztek.xend.feature.games.domain.usecase

import com.noztek.xend.feature.games.domain.model.GamesOverviewModel
import com.noztek.xend.feature.games.domain.repository.GamesRepository

class GetGamesOverviewUseCase(
    private val repository: GamesRepository,
) {
    suspend operator fun invoke(): GamesOverviewModel = repository.getOverview()
}
