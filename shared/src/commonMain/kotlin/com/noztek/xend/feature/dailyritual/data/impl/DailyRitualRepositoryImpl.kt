package com.noztek.xend.feature.dailyritual.data.impl

import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.dailyritual.data.remote.DailyRitualApi
import com.noztek.xend.feature.dailyritual.data.remote.DailyRitualAssignedDto
import com.noztek.xend.feature.dailyritual.data.remote.DailyRitualOverviewDto
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualAssignedModel
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualStatusModel
import com.noztek.xend.feature.dailyritual.domain.repository.DailyRitualRepository

class DailyRitualRepositoryImpl(
    private val authSessionDao: AuthSessionDao,
    private val api: DailyRitualApi,
) : DailyRitualRepository {
    override suspend fun getOverview(spaceId: String): DailyRitualStatusModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        return api.getOverview(session.accessToken, spaceId).toModel()
    }

    override suspend fun submit(spaceId: String, assignmentId: String, textResponse: String?) {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        api.submit(
            accessToken = session.accessToken,
            spaceId = spaceId,
            assignmentId = assignmentId,
            textResponse = textResponse,
        )
    }

    override suspend fun submitImage(spaceId: String, assignmentId: String, image: PickedImageData) {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        api.submitImage(
            accessToken = session.accessToken,
            spaceId = spaceId,
            assignmentId = assignmentId,
            image = image,
        )
    }
}

private fun DailyRitualOverviewDto.toModel(): DailyRitualStatusModel {
    return DailyRitualStatusModel(
        relationshipSpaceId = relationshipSpaceId,
        ritualDate = ritualDate,
        todayRitual = todayRitual?.toModel(),
        history = history.map { it.toModel() },
    )
}

private fun DailyRitualAssignedDto.toModel(): DailyRitualAssignedModel {
    return DailyRitualAssignedModel(
        assignmentId = assignmentId,
        ritualDate = ritualDate,
        title = title,
        description = description,
        category = category,
        iconKey = iconKey,
        rewardPoints = rewardPoints,
        submissionType = submissionType,
        targetType = targetType,
        completionRule = completionRule,
        suggestedTime = suggestedTime,
        completed = completed,
        submittedByMe = submittedByMe,
        submittedCount = submittedCount,
        requiredCount = requiredCount,
        canSubmit = canSubmit,
    )
}
