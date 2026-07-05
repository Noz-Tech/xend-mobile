package com.noztek.xend.feature.games.data.impl

import com.noztek.xend.feature.games.domain.model.CoupleGameModel
import com.noztek.xend.feature.games.domain.model.GameAccent
import com.noztek.xend.feature.games.domain.model.GameCategory
import com.noztek.xend.feature.games.domain.model.GamesOverviewModel
import com.noztek.xend.feature.games.domain.repository.GamesRepository

class MockGamesRepository : GamesRepository {
    override suspend fun getOverview(): GamesOverviewModel {
        val featured = CoupleGameModel(
            id = "love-letter",
            title = "Love Letter",
            description = "Answer sweet questions and write a little love note.",
            category = GameCategory.Romantic,
            accent = GameAccent.Rose,
            badge = "Tonight's Pick",
        )

        return GamesOverviewModel(
            playedTogetherDays = 12,
            featuredGame = featured,
            games = listOf(
                CoupleGameModel(
                    id = "would-you-rather",
                    title = "Would You Rather",
                    description = "Pick your preference and start fun debates.",
                    category = GameCategory.Quick,
                    accent = GameAccent.Lavender,
                ),
                CoupleGameModel(
                    id = "who-knows-me-better",
                    title = "Who Knows Me Better",
                    description = "See how well your partner really knows you.",
                    category = GameCategory.Romantic,
                    accent = GameAccent.Rose,
                ),
                CoupleGameModel(
                    id = "truth-or-dare",
                    title = "Truth or Dare",
                    description = "Spice things up with truths and dares.",
                    category = GameCategory.Funny,
                    accent = GameAccent.Peach,
                ),
                CoupleGameModel(
                    id = "love-quiz",
                    title = "Love Quiz",
                    description = "Answer questions and find out your love score.",
                    category = GameCategory.Romantic,
                    accent = GameAccent.Lavender,
                ),
                CoupleGameModel(
                    id = "this-or-that",
                    title = "This or That",
                    description = "Quick choices, big conversations.",
                    category = GameCategory.Quick,
                    accent = GameAccent.Peach,
                ),
                CoupleGameModel(
                    id = "spin-the-wheel",
                    title = "Spin the Wheel",
                    description = "Let fate decide your next move.",
                    category = GameCategory.Funny,
                    accent = GameAccent.Mint,
                ),
            ),
        )
    }
}
