package com.noztek.xend.feature.challenges.data.impl

import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.feature.challenges.data.remote.ChallengeItemDto
import com.noztek.xend.feature.challenges.data.remote.ChallengeTemplateDto
import com.noztek.xend.feature.challenges.data.remote.ChallengesApi
import com.noztek.xend.feature.challenges.data.remote.ChallengesOverviewDto
import com.noztek.xend.feature.challenges.domain.model.ChallengeAssignmentModel
import com.noztek.xend.feature.challenges.domain.model.ChallengeCategory
import com.noztek.xend.feature.challenges.domain.model.ChallengeSubmissionImageModel
import com.noztek.xend.feature.challenges.domain.model.ChallengeStatus
import com.noztek.xend.feature.challenges.domain.model.ChallengeSubmissionType
import com.noztek.xend.feature.challenges.domain.model.ChallengeTemplateModel
import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel
import com.noztek.xend.feature.challenges.domain.repository.ChallengesRepository

class ChallengesRepositoryImpl(
    private val authSessionDao: AuthSessionDao,
    private val api: ChallengesApi,
) : ChallengesRepository {
    override suspend fun getOverview(spaceId: String, partnerNameFallback: String): ChallengesOverviewModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val overview = api.getOverview(session.accessToken, spaceId)
        val templates = api.getTemplates(session.accessToken, spaceId)
        return overview.toModel(templates = templates, partnerNameFallback = partnerNameFallback)
    }

    override suspend fun createChallenge(
        spaceId: String,
        templateId: String,
        note: String?,
        partnerNameFallback: String,
    ): ChallengesOverviewModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val overview = api.createChallenge(session.accessToken, spaceId, templateId, note)
        val templates = api.getTemplates(session.accessToken, spaceId)
        return overview.toModel(templates = templates, partnerNameFallback = partnerNameFallback)
    }

    override suspend fun acceptChallenge(
        spaceId: String,
        challengeId: String,
        partnerNameFallback: String,
    ): ChallengesOverviewModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val overview = api.acceptChallenge(session.accessToken, spaceId, challengeId)
        val templates = api.getTemplates(session.accessToken, spaceId)
        return overview.toModel(templates = templates, partnerNameFallback = partnerNameFallback)
    }

    override suspend fun declineChallenge(
        spaceId: String,
        challengeId: String,
        partnerNameFallback: String,
    ): ChallengesOverviewModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val overview = api.declineChallenge(session.accessToken, spaceId, challengeId)
        val templates = api.getTemplates(session.accessToken, spaceId)
        return overview.toModel(templates = templates, partnerNameFallback = partnerNameFallback)
    }

    override suspend fun completeChallenge(
        spaceId: String,
        challengeId: String,
        textResponse: String?,
        partnerNameFallback: String,
    ): ChallengesOverviewModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val overview = api.completeChallenge(session.accessToken, spaceId, challengeId, textResponse)
        val templates = api.getTemplates(session.accessToken, spaceId)
        return overview.toModel(templates = templates, partnerNameFallback = partnerNameFallback)
    }

    override suspend fun completeChallengeImage(
        spaceId: String,
        challengeId: String,
        image: PickedImageData,
        partnerNameFallback: String,
    ): ChallengesOverviewModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val overview = api.completeChallengeImage(session.accessToken, spaceId, challengeId, image)
        val templates = api.getTemplates(session.accessToken, spaceId)
        return overview.toModel(templates = templates, partnerNameFallback = partnerNameFallback)
    }

    override suspend fun getSubmissionImage(
        spaceId: String,
        challengeId: String,
    ): ChallengeSubmissionImageModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        return ChallengeSubmissionImageModel(
            challengeId = challengeId,
            bitmap = api.getSubmissionImage(session.accessToken, spaceId, challengeId),
        )
    }
}

private fun ChallengesOverviewDto.toModel(
    templates: List<ChallengeTemplateDto>,
    partnerNameFallback: String,
): ChallengesOverviewModel {
    val mappedIncoming = incoming.map { it.toModel() }
    val mappedSent = sent.map { it.toModel() }
    val mappedHistory = history.map { it.toModel() }

    val resolvedPartnerName = listOfNotNull(
        mappedIncoming.firstOrNull()?.senderDisplayName?.takeIf { it.isNotBlank() },
        mappedSent.firstOrNull()?.receiverDisplayName?.takeIf { it.isNotBlank() },
        mappedHistory.firstOrNull()?.let { historyItem ->
            historyItem.receiverDisplayName.takeIf { it.isNotBlank() && it != historyItem.senderDisplayName }
                ?: historyItem.senderDisplayName.takeIf { it.isNotBlank() }
        },
        partnerNameFallback.takeIf { it.isNotBlank() },
    ).firstOrNull() ?: "Your partner"

    return ChallengesOverviewModel(
        relationshipSpaceId = relationshipSpaceId,
        partnerName = resolvedPartnerName,
        templates = templates.map { it.toModel() },
        incoming = mappedIncoming,
        sent = mappedSent,
        history = mappedHistory,
    )
}

private fun ChallengeTemplateDto.toModel(): ChallengeTemplateModel {
    return ChallengeTemplateModel(
        templateId = templateId,
        slug = slug,
        title = title,
        description = description,
        category = category.toChallengeCategory(),
        submissionType = submissionType.toChallengeSubmissionType(),
        rewardPoints = defaultPoints,
        expiryLabel = expiryHours?.let { "$it h window" },
    )
}

private fun ChallengeItemDto.toModel(): ChallengeAssignmentModel {
    return ChallengeAssignmentModel(
        challengeId = challengeId,
        templateId = templateId,
        title = title,
        description = description,
        category = category.toChallengeCategory(),
        submissionType = submissionType.toChallengeSubmissionType(),
        senderDisplayName = senderDisplayName,
        receiverDisplayName = receiverDisplayName,
        rewardPoints = rewardPoints,
        note = note?.trim()?.takeIf { it.isNotBlank() },
        status = status.toChallengeStatus(),
        assignedLevel = assignedLevel,
        expiresAtLabel = expiresAt?.toChallengeDateTimeLabel(prefix = "Ends"),
        createdAtLabel = createdAt.toChallengeDateTimeLabel(prefix = "Sent"),
        canAccept = canAccept,
        canDecline = canDecline,
        canComplete = canComplete,
        submittedByMe = submittedByMe,
        submissionTextResponse = submissionTextResponse?.trim()?.takeIf { it.isNotBlank() },
        hasSubmissionImage = hasSubmissionImage,
    )
}

private fun String.toChallengeCategory(): ChallengeCategory {
    return when (lowercase()) {
        "desire" -> ChallengeCategory.Desire
        "private" -> ChallengeCategory.Private
        "bold" -> ChallengeCategory.Bold
        "devotion" -> ChallengeCategory.Devotion
        else -> ChallengeCategory.Soft
    }
}

private fun String.toChallengeSubmissionType(): ChallengeSubmissionType {
    return when (lowercase()) {
        "image" -> ChallengeSubmissionType.Image
        "text" -> ChallengeSubmissionType.Text
        else -> ChallengeSubmissionType.None
    }
}

private fun String.toChallengeStatus(): ChallengeStatus {
    return when (lowercase()) {
        "accepted" -> ChallengeStatus.Accepted
        "completed" -> ChallengeStatus.Completed
        "declined" -> ChallengeStatus.Declined
        "expired" -> ChallengeStatus.Expired
        "cancelled" -> ChallengeStatus.Cancelled
        else -> ChallengeStatus.Sent
    }
}

private fun Long.toChallengeDateTimeLabel(prefix: String): String {
    val hours = ((kotlin.time.Clock.System.now().epochSeconds - this) / 3600L).coerceAtLeast(0L)
    val ageLabel = when {
        hours < 1L -> "just now"
        hours < 24L -> "$hours h ago"
        else -> "${hours / 24L} d ago"
    }
    return "$prefix $ageLabel"
}
