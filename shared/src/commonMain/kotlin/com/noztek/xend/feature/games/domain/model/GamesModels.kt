package com.noztek.xend.feature.games.domain.model

data class GamesOverviewModel(
    val playedTogetherDays: Int,
    val featuredGame: CoupleGameModel,
    val games: List<CoupleGameModel>,
)

data class CoupleGameModel(
    val id: String,
    val title: String,
    val description: String,
    val category: GameCategory,
    val accent: GameAccent,
    val badge: String? = null,
)

enum class GameCategory {
    All,
    Quick,
    Romantic,
    Funny,
}

enum class GameAccent {
    Rose,
    Lavender,
    Peach,
    Mint,
}
