package com.noztek.xend.feature.challenges.data.impl

import com.noztek.xend.feature.challenges.domain.model.ChallengeAccent
import com.noztek.xend.feature.challenges.domain.model.ChallengeActionStyle
import com.noztek.xend.feature.challenges.domain.model.ChallengeAudience
import com.noztek.xend.feature.challenges.domain.model.ChallengeCategory
import com.noztek.xend.feature.challenges.domain.model.ChallengeIdeaModel
import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel
import com.noztek.xend.feature.challenges.domain.model.DailyChallengeModel
import com.noztek.xend.feature.challenges.domain.repository.ChallengesRepository

class MockChallengesRepository : ChallengesRepository {
    override suspend fun getOverview(): ChallengesOverviewModel {
        return ChallengesOverviewModel(
            dailyChallenge = DailyChallengeModel(
                title = "Send a sweet message",
                description = "Send a message that makes your partner smile.",
                completedCount = 0,
                totalCount = 2,
                bondPoints = 50,
                hoursLeft = 23,
            ),
            ideas = listOf(
                ChallengeIdeaModel(
                    id = "good-morning-note",
                    title = "Good morning note",
                    description = "Send a cute good morning message.",
                    bondPoints = 20,
                    category = ChallengeCategory.Romantic,
                    audience = ChallengeAudience.ForYou,
                    accent = ChallengeAccent.Sunrise,
                    actionStyle = ChallengeActionStyle.Primary,
                ),
                ChallengeIdeaModel(
                    id = "take-a-break-together",
                    title = "Take a break together",
                    description = "Take a break and relax together.",
                    bondPoints = 30,
                    category = ChallengeCategory.Supportive,
                    audience = ChallengeAudience.ForYou,
                    accent = ChallengeAccent.Lavender,
                    actionStyle = ChallengeActionStyle.Add,
                ),
                ChallengeIdeaModel(
                    id = "give-a-genuine-compliment",
                    title = "Give a genuine compliment",
                    description = "Make your partner feel special.",
                    bondPoints = 20,
                    category = ChallengeCategory.Supportive,
                    audience = ChallengeAudience.ForThem,
                    accent = ChallengeAccent.Mint,
                    actionStyle = ChallengeActionStyle.Add,
                ),
                ChallengeIdeaModel(
                    id = "surprise-your-partner",
                    title = "Surprise your partner",
                    description = "Do something unexpected.",
                    bondPoints = 40,
                    category = ChallengeCategory.Fun,
                    audience = ChallengeAudience.ForThem,
                    accent = ChallengeAccent.Rose,
                    actionStyle = ChallengeActionStyle.Add,
                ),
            ),
            progressMessage = "Complete more challenges together and earn more Bond Points.",
        )
    }
}
