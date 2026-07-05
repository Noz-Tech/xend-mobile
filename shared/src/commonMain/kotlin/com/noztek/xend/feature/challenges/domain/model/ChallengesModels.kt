package com.noztek.xend.feature.challenges.domain.model

data class ChallengesOverviewModel(
    val dailyChallenge: DailyChallengeModel,
    val ideas: List<ChallengeIdeaModel>,
    val progressMessage: String,
)

data class DailyChallengeModel(
    val title: String,
    val description: String,
    val completedCount: Int,
    val totalCount: Int,
    val bondPoints: Int,
    val hoursLeft: Int,
)

data class ChallengeIdeaModel(
    val id: String,
    val title: String,
    val description: String,
    val bondPoints: Int,
    val category: ChallengeCategory,
    val audience: ChallengeAudience,
    val accent: ChallengeAccent,
    val actionStyle: ChallengeActionStyle,
)

enum class ChallengeAudience {
    ForYou,
    ForThem,
}

enum class ChallengeCategory {
    All,
    Romantic,
    Fun,
    Supportive,
}

enum class ChallengeAccent {
    Sunrise,
    Lavender,
    Mint,
    Rose,
}

enum class ChallengeActionStyle {
    Primary,
    Add,
}
