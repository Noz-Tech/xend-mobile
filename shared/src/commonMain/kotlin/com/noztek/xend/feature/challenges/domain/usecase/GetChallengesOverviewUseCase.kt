package com.noztek.xend.feature.challenges.domain.usecase

import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel
import com.noztek.xend.feature.challenges.domain.repository.ChallengesRepository

class GetChallengesOverviewUseCase(
    private val repository: ChallengesRepository,
) {
    suspend operator fun invoke(): ChallengesOverviewModel = repository.getOverview()
}
