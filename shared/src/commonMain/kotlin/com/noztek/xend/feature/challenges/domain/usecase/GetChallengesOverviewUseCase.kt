package com.noztek.xend.feature.challenges.domain.usecase

import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.challenges.domain.model.ChallengeSubmissionImageModel
import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel
import com.noztek.xend.feature.challenges.domain.repository.ChallengesRepository
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.GetDefaultSpaceHeroUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase

class GetChallengesOverviewUseCase(
    private val repository: ChallengesRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getDefaultSpaceHero: GetDefaultSpaceHeroUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
) {
    suspend operator fun invoke(): ChallengesOverviewModel {
        val context = resolveChallengeContext(
            getDefaultRelationshipSpace = getDefaultRelationshipSpace,
            getDefaultSpaceHero = getDefaultSpaceHero,
            syncRelationshipSpaces = syncRelationshipSpaces,
        )
        return repository.getOverview(
            spaceId = context.space.relationshipSpaceId,
            partnerNameFallback = context.partnerName,
        )
    }
}

class SendChallengeUseCase(
    private val repository: ChallengesRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getDefaultSpaceHero: GetDefaultSpaceHeroUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
) {
    suspend operator fun invoke(templateId: String, note: String?): ChallengesOverviewModel {
        val context = resolveChallengeContext(
            getDefaultRelationshipSpace = getDefaultRelationshipSpace,
            getDefaultSpaceHero = getDefaultSpaceHero,
            syncRelationshipSpaces = syncRelationshipSpaces,
        )
        val overview = repository.createChallenge(
            spaceId = context.space.relationshipSpaceId,
            templateId = templateId,
            note = note,
            partnerNameFallback = context.partnerName,
        )
        runCatching { syncRelationshipSpaces() }
        return overview
    }
}

class AcceptChallengeUseCase(
    private val repository: ChallengesRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getDefaultSpaceHero: GetDefaultSpaceHeroUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
) {
    suspend operator fun invoke(challengeId: String): ChallengesOverviewModel {
        val context = resolveChallengeContext(
            getDefaultRelationshipSpace = getDefaultRelationshipSpace,
            getDefaultSpaceHero = getDefaultSpaceHero,
            syncRelationshipSpaces = syncRelationshipSpaces,
        )
        val overview = repository.acceptChallenge(
            spaceId = context.space.relationshipSpaceId,
            challengeId = challengeId,
            partnerNameFallback = context.partnerName,
        )
        runCatching { syncRelationshipSpaces() }
        return overview
    }
}

class DeclineChallengeUseCase(
    private val repository: ChallengesRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getDefaultSpaceHero: GetDefaultSpaceHeroUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
) {
    suspend operator fun invoke(challengeId: String): ChallengesOverviewModel {
        val context = resolveChallengeContext(
            getDefaultRelationshipSpace = getDefaultRelationshipSpace,
            getDefaultSpaceHero = getDefaultSpaceHero,
            syncRelationshipSpaces = syncRelationshipSpaces,
        )
        val overview = repository.declineChallenge(
            spaceId = context.space.relationshipSpaceId,
            challengeId = challengeId,
            partnerNameFallback = context.partnerName,
        )
        runCatching { syncRelationshipSpaces() }
        return overview
    }
}

class CompleteChallengeUseCase(
    private val repository: ChallengesRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getDefaultSpaceHero: GetDefaultSpaceHeroUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
) {
    suspend operator fun invoke(challengeId: String, textResponse: String? = null): ChallengesOverviewModel {
        val context = resolveChallengeContext(
            getDefaultRelationshipSpace = getDefaultRelationshipSpace,
            getDefaultSpaceHero = getDefaultSpaceHero,
            syncRelationshipSpaces = syncRelationshipSpaces,
        )
        val overview = repository.completeChallenge(
            spaceId = context.space.relationshipSpaceId,
            challengeId = challengeId,
            textResponse = textResponse,
            partnerNameFallback = context.partnerName,
        )
        runCatching { syncRelationshipSpaces() }
        return overview
    }

    suspend fun submitImage(challengeId: String, image: PickedImageData): ChallengesOverviewModel {
        val context = resolveChallengeContext(
            getDefaultRelationshipSpace = getDefaultRelationshipSpace,
            getDefaultSpaceHero = getDefaultSpaceHero,
            syncRelationshipSpaces = syncRelationshipSpaces,
        )
        val overview = repository.completeChallengeImage(
            spaceId = context.space.relationshipSpaceId,
            challengeId = challengeId,
            image = image,
            partnerNameFallback = context.partnerName,
        )
        runCatching { syncRelationshipSpaces() }
        return overview
    }
}

class GetChallengeSubmissionImageUseCase(
    private val repository: ChallengesRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
) {
    suspend operator fun invoke(challengeId: String): ChallengeSubmissionImageModel {
        val space = resolveDefaultSpace(
            getDefaultRelationshipSpace = getDefaultRelationshipSpace,
            syncRelationshipSpaces = syncRelationshipSpaces,
        )
        return repository.getSubmissionImage(
            spaceId = space.relationshipSpaceId,
            challengeId = challengeId,
        )
    }
}

private data class ChallengeContext(
    val space: RelationshipSpaceCardModel,
    val partnerName: String,
)

private suspend fun resolveChallengeContext(
    getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    getDefaultSpaceHero: GetDefaultSpaceHeroUseCase,
    syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
): ChallengeContext {
    val space = resolveDefaultSpace(
        getDefaultRelationshipSpace = getDefaultRelationshipSpace,
        syncRelationshipSpaces = syncRelationshipSpaces,
    )
    val partnerName = getDefaultSpaceHero(space)?.partnerName?.takeIf { it.isNotBlank() } ?: "Your partner"
    return ChallengeContext(space = space, partnerName = partnerName)
}

private suspend fun resolveDefaultSpace(
    getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
): RelationshipSpaceCardModel {
    getDefaultRelationshipSpace()?.let { return it }
    runCatching { syncRelationshipSpaces() }
    return requireNotNull(getDefaultRelationshipSpace()) { "No active space yet" }
}
