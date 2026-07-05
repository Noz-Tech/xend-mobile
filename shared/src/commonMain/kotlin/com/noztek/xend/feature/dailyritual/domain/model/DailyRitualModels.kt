package com.noztek.xend.feature.dailyritual.domain.model

data class DailyRitualOverviewModel(
    val completedCount: Int,
    val totalCount: Int,
    val summaryTitle: String,
    val summaryBody: String,
    val rituals: List<DailyRitualItemModel>,
    val streakDays: Int,
    val streakMessage: String,
)

data class DailyRitualItemModel(
    val title: String,
    val description: String,
    val kind: RitualItemKind,
    val completed: Boolean,
)

enum class RitualItemKind {
    MorningMessage,
    CheckIn,
    GratitudeMoment,
    SharePhoto,
    GoodNightMessage,
}
