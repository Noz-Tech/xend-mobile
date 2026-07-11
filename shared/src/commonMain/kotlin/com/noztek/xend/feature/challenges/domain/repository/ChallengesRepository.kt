package com.noztek.xend.feature.challenges.domain.repository

import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.challenges.domain.model.ChallengeSubmissionImageModel
import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel

interface ChallengesRepository {
    suspend fun getOverview(spaceId: String, partnerNameFallback: String): ChallengesOverviewModel
    suspend fun createChallenge(
        spaceId: String,
        templateId: String,
        note: String?,
        partnerNameFallback: String,
    ): ChallengesOverviewModel
    suspend fun acceptChallenge(
        spaceId: String,
        challengeId: String,
        partnerNameFallback: String,
    ): ChallengesOverviewModel
    suspend fun declineChallenge(
        spaceId: String,
        challengeId: String,
        partnerNameFallback: String,
    ): ChallengesOverviewModel
    suspend fun completeChallenge(
        spaceId: String,
        challengeId: String,
        textResponse: String?,
        partnerNameFallback: String,
    ): ChallengesOverviewModel
    suspend fun completeChallengeImage(
        spaceId: String,
        challengeId: String,
        image: PickedImageData,
        partnerNameFallback: String,
    ): ChallengesOverviewModel
    suspend fun getSubmissionImage(
        spaceId: String,
        challengeId: String,
    ): ChallengeSubmissionImageModel
}
