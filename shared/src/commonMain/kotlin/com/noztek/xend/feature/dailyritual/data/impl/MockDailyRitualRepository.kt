package com.noztek.xend.feature.dailyritual.data.impl

import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualOverviewModel
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualItemModel
import com.noztek.xend.feature.dailyritual.domain.model.RitualItemKind
import com.noztek.xend.feature.dailyritual.domain.repository.DailyRitualRepository

class MockDailyRitualRepository : DailyRitualRepository {
    override suspend fun getOverview(): DailyRitualOverviewModel {
        return DailyRitualOverviewModel(
            completedCount = 3,
            totalCount = 5,
            summaryTitle = "You're doing great!",
            summaryBody = "Keep building beautiful habits together. 💕",
            rituals = listOf(
                DailyRitualItemModel(
                    title = "Good morning message",
                    description = "Send a sweet good morning message to start the day.",
                    kind = RitualItemKind.MorningMessage,
                    completed = true,
                ),
                DailyRitualItemModel(
                    title = "Check in",
                    description = "Ask how each other is feeling.",
                    kind = RitualItemKind.CheckIn,
                    completed = true,
                ),
                DailyRitualItemModel(
                    title = "Gratitude moment",
                    description = "Share one thing you're grateful for today.",
                    kind = RitualItemKind.GratitudeMoment,
                    completed = true,
                ),
                DailyRitualItemModel(
                    title = "Share a photo",
                    description = "Send a photo of your day.",
                    kind = RitualItemKind.SharePhoto,
                    completed = false,
                ),
                DailyRitualItemModel(
                    title = "Good night message",
                    description = "End the day with a sweet good night message.",
                    kind = RitualItemKind.GoodNightMessage,
                    completed = false,
                ),
            ),
            streakDays = 12,
            streakMessage = "Keep it up, lovebirds! 💕",
        )
    }
}
