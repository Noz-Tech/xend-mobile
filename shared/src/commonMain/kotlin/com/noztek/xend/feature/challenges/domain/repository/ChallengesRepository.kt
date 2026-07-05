package com.noztek.xend.feature.challenges.domain.repository

import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel

interface ChallengesRepository {
    suspend fun getOverview(): ChallengesOverviewModel
}
