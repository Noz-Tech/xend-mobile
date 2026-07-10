package com.noztek.xend.feature.dailyritual.domain.model

data class DailyRitualOverviewModel(
    val completedCount: Int,
    val totalCount: Int,
    val summaryTitle: String,
    val summaryBody: String,
    val todayRitual: DailyRitualTodayModel? = null,
    val history: List<DailyRitualHistoryItemModel>,
    val streakDays: Int,
    val streakMessage: String,
)

data class DailyRitualTodayModel(
    val assignmentId: String,
    val title: String,
    val description: String,
    val kind: RitualItemKind,
    val rewardPoints: Int,
    val suggestedTime: String? = null,
    val completed: Boolean,
    val submissionType: String,
    val canSubmit: Boolean,
    val submittedByMe: Boolean,
    val submittedCount: Int,
    val requiredCount: Int,
    val statusLabel: String? = null,
)

data class DailyRitualHistoryItemModel(
    val title: String,
    val description: String,
    val kind: RitualItemKind,
    val supportingLabel: String? = null,
    val completed: Boolean,
)

data class DailyRitualStatusModel(
    val relationshipSpaceId: String,
    val ritualDate: String,
    val todayRitual: DailyRitualAssignedModel? = null,
    val history: List<DailyRitualAssignedModel>,
)

data class DailyRitualAssignedModel(
    val assignmentId: String,
    val ritualDate: String,
    val title: String,
    val description: String,
    val category: String,
    val iconKey: String,
    val rewardPoints: Int,
    val submissionType: String,
    val targetType: String,
    val completionRule: String,
    val suggestedTime: String? = null,
    val completed: Boolean,
    val submittedByMe: Boolean,
    val submittedCount: Int,
    val requiredCount: Int,
    val canSubmit: Boolean,
)

enum class RitualItemKind {
    MorningMessage,
    CheckIn,
    GratitudeMoment,
    SharePhoto,
    GoodNightMessage,
}
