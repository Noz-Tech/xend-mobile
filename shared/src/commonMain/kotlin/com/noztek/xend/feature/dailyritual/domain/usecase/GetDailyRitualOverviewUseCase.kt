package com.noztek.xend.feature.dailyritual.domain.usecase

import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualAssignedModel
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualHistoryItemModel
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualOverviewModel
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualStatusModel
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualTodayModel
import com.noztek.xend.feature.dailyritual.domain.model.RitualItemKind
import com.noztek.xend.feature.dailyritual.domain.repository.DailyRitualRepository
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase

class GetDailyRitualOverviewUseCase(
    private val repository: DailyRitualRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
) {
    suspend operator fun invoke(): DailyRitualOverviewModel {
        val space = resolveDefaultSpace()
        val status = repository.getOverview(space.relationshipSpaceId)
        return status.toOverviewModel()
    }

    private suspend fun resolveDefaultSpace(): RelationshipSpaceCardModel {
        getDefaultRelationshipSpace()?.let { return it }
        runCatching { syncRelationshipSpaces() }
        return requireNotNull(getDefaultRelationshipSpace()) { "No active space yet" }
    }
}

class SubmitDailyRitualUseCase(
    private val repository: DailyRitualRepository,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val getOverview: GetDailyRitualOverviewUseCase,
) {
    suspend operator fun invoke(assignmentId: String, textResponse: String? = null): DailyRitualOverviewModel {
        val space = resolveDefaultSpace()
        repository.submit(
            spaceId = space.relationshipSpaceId,
            assignmentId = assignmentId,
            textResponse = textResponse,
        )
        runCatching { syncRelationshipSpaces() }
        return getOverview()
    }

    suspend fun submitImage(assignmentId: String, image: PickedImageData): DailyRitualOverviewModel {
        val space = resolveDefaultSpace()
        repository.submitImage(
            spaceId = space.relationshipSpaceId,
            assignmentId = assignmentId,
            image = image,
        )
        runCatching { syncRelationshipSpaces() }
        return getOverview()
    }

    private suspend fun resolveDefaultSpace(): RelationshipSpaceCardModel {
        getDefaultRelationshipSpace()?.let { return it }
        runCatching { syncRelationshipSpaces() }
        return requireNotNull(getDefaultRelationshipSpace()) { "No active space yet" }
    }
}

private fun DailyRitualAssignedModel.toTodayModel(): DailyRitualTodayModel {
    return DailyRitualTodayModel(
        assignmentId = assignmentId,
        title = title,
        description = description,
        kind = toRitualItemKind(),
        rewardPoints = rewardPoints,
        suggestedTime = suggestedTime?.replaceFirstChar { it.uppercase() },
        completed = completed,
        submissionType = submissionType,
        canSubmit = canSubmit,
        submittedByMe = submittedByMe,
        submittedCount = submittedCount,
        requiredCount = requiredCount,
        statusLabel = when {
            completed -> "Completed for today."
            submittedByMe && completionRule == "both_partners" && submittedCount < requiredCount -> {
                "Your part is done. Waiting for your partner."
            }
            !canSubmit && targetType == "one_partner" -> "Assigned to your partner today."
            submissionType == "image" && canSubmit -> "Share one photo to complete today's ritual."
            else -> null
        },
    )
}

private fun DailyRitualAssignedModel.toHistoryItem(): DailyRitualHistoryItemModel {
    return DailyRitualHistoryItemModel(
        title = title,
        description = description,
        kind = toRitualItemKind(),
        supportingLabel = ritualDate.toFriendlyDateLabel(),
        completed = completed,
    )
}

private fun DailyRitualStatusModel.toOverviewModel(): DailyRitualOverviewModel {
    return DailyRitualOverviewModel(
        completedCount = if (todayRitual?.completed == true) 1 else 0,
        totalCount = if (todayRitual != null) 1 else 0,
        summaryTitle = if (todayRitual != null) {
            "Today's ritual is ready"
        } else {
            "No ritual scheduled"
        },
        summaryBody = if (todayRitual != null) {
            "One ritual, chosen by the server for your current couple level."
        } else {
            "No ritual is available for your current level yet."
        },
        todayRitual = todayRitual?.toTodayModel(),
        history = history.map { it.toHistoryItem() },
        streakDays = history.takeWhile { it.completed }.count() + if (todayRitual?.completed == true) 1 else 0,
        streakMessage = if (todayRitual?.completed == true) {
            "You completed today's ritual. Keep the rhythm going."
        } else {
            "One ritual a day keeps your shared rhythm steady."
        },
    )
}

private fun DailyRitualAssignedModel.toRitualItemKind(): RitualItemKind {
    return when (iconKey.lowercase()) {
        "sun" -> RitualItemKind.MorningMessage
        "chat" -> RitualItemKind.CheckIn
        "sparkles" -> RitualItemKind.GratitudeMoment
        "camera" -> RitualItemKind.SharePhoto
        "moon" -> RitualItemKind.GoodNightMessage
        else -> when (category.lowercase()) {
            "care" -> RitualItemKind.CheckIn
            "memory" -> RitualItemKind.SharePhoto
            "connection" -> RitualItemKind.MorningMessage
            else -> RitualItemKind.GratitudeMoment
        }
    }
}

private fun String.toFriendlyDateLabel(): String {
    val parts = split("-")
    if (parts.size != 3) return this

    val monthName = when (parts[1].toIntOrNull()) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> return this
    }
    val day = parts[2].toIntOrNull() ?: return this
    return "$monthName $day"
}
