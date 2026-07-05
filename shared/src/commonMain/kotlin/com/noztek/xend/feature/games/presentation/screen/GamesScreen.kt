package com.noztek.xend.feature.games.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ChatBubbleLeftRight
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.FaceSmile
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.Sparkles
import com.composables.icons.heroicons.solid.Fire
import com.noztek.xend.core.ui.theme.XendPalette
import com.noztek.xend.core.ui.theme.XendTheme
import com.noztek.xend.feature.games.domain.model.CoupleGameModel
import com.noztek.xend.feature.games.domain.model.GameAccent
import com.noztek.xend.feature.games.domain.model.GameCategory
import com.noztek.xend.feature.games.domain.model.GamesOverviewModel
import com.noztek.xend.feature.games.presentation.viewmodel.GamesViewModel
import org.koin.compose.koinInject

@Composable
fun GamesScreen(
    onBackClick: () -> Unit = {},
    onGameClick: (String) -> Unit = {},
    viewModel: GamesViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val palette = XendTheme.palette

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.background,
                        palette.backgroundGlow,
                    ),
                ),
            ),
    ) {
        when {
            state.isLoading && state.overview == null -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = palette.primary,
                )
            }

            state.overview != null -> {
                GamesContent(
                    overview = requireNotNull(state.overview),
                    selectedCategory = state.selectedCategory,
                    palette = palette,
                    onBackClick = onBackClick,
                    onCategorySelected = viewModel::onCategorySelected,
                    onGameClick = onGameClick,
                )
            }

            else -> {
                Text(
                    text = state.message ?: "Unable to load games.",
                    color = palette.mutedInk,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                )
            }
        }
    }
}

@Composable
private fun GamesContent(
    overview: GamesOverviewModel,
    selectedCategory: GameCategory,
    palette: XendPalette,
    onBackClick: () -> Unit,
    onCategorySelected: (GameCategory) -> Unit,
    onGameClick: (String) -> Unit,
) {
    val filteredGames = when (selectedCategory) {
        GameCategory.All -> overview.games
        else -> overview.games.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeaderSection(
                palette = palette,
                onBackClick = onBackClick,
            )
        }
        item {
            GamesHeroSection(
                playedTogetherDays = overview.playedTogetherDays,
                palette = palette,
            )
        }
        item {
            CategoryTabs(
                selectedCategory = selectedCategory,
                palette = palette,
                onCategorySelected = onCategorySelected,
            )
        }
        item {
            FeaturedGameCard(
                game = overview.featuredGame,
                palette = palette,
                onClick = { onGameClick(overview.featuredGame.id) },
            )
        }
        items(filteredGames.chunked(2)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                rowItems.forEach { item ->
                    GameCard(
                        game = item,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = { onGameClick(item.id) },
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    palette: XendPalette,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Couple Games",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                    ),
                    color = palette.ink,
                )
                HeartCluster(
                    palette = palette,
                )
            }
            Text(
                text = "Play, laugh, and get closer together.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                ),
                color = palette.mutedInk,
            )
        }

        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onBackClick),
            shape = RoundedCornerShape(15.dp),
            color = Color.White.copy(alpha = 0.82f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = palette.primarySoft,
            ),
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.ArrowLeft,
                    contentDescription = "Back",
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun HeartCluster(
    palette: XendPalette,
) {
    Box(
        modifier = Modifier.size(width = 20.dp, height = 16.dp),
    ) {
        Icon(
            imageVector = Heroicons.Outline.Heart,
            contentDescription = null,
            tint = palette.primary.copy(alpha = 0.88f),
            modifier = Modifier
                .size(11.dp)
                .align(Alignment.BottomStart),
        )
        Icon(
            imageVector = Heroicons.Outline.Heart,
            contentDescription = null,
            tint = palette.primaryBright,
            modifier = Modifier
                .size(13.dp)
                .align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun GamesHeroSection(
    playedTogetherDays: Int,
    palette: XendPalette,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlayedTogetherPill(
                playedTogetherDays = playedTogetherDays,
                palette = palette,
            )
        }
        GamesHeroArt(
            palette = palette,
            modifier = Modifier.size(86.dp),
        )
    }
}

@Composable
private fun PlayedTogetherPill(
    playedTogetherDays: Int,
    palette: XendPalette,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        border = BorderStroke(0.5.dp, palette.outline),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            palette.primarySoft.copy(alpha = 0.95f),
                            Color.White,
                        ),
                    ),
                    shape = RoundedCornerShape(999.dp),
                )
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Heroicons.Solid.Fire,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Played together",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "$playedTogetherDays days",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.primary,
            )
        }
    }
}

@Composable
private fun GamesHeroArt(
    palette: XendPalette,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        SmallFloatingAccent(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 12.dp),
            tint = palette.primary,
            size = 8.dp,
        )
        SmallFloatingAccent(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 6.dp, top = 18.dp),
            tint = palette.primary,
            size = 9.dp,
        )
        SmallFloatingAccent(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 0.dp, top = 14.dp),
            tint = palette.lavender,
            size = 6.dp,
        )
        SmallFloatingAccent(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 10.dp),
            tint = palette.lavender,
            size = 6.dp,
        )
        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp),
            shape = CircleShape,
            color = palette.primarySoft,
            shadowElevation = 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.Heart,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 12.dp)
                .size(38.dp),
            shape = CircleShape,
            color = palette.lavenderSoft,
            shadowElevation = 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.FaceSmile,
                    contentDescription = null,
                    tint = palette.lavender,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SmallFloatingAccent(
    modifier: Modifier,
    tint: Color,
    size: Dp,
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = tint.copy(alpha = 0.14f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Heroicons.Outline.Heart,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size * 0.58f),
            )
        }
    }
}

@Composable
private fun CategoryTabs(
    selectedCategory: GameCategory,
    palette: XendPalette,
    onCategorySelected: (GameCategory) -> Unit,
) {
    val categories = listOf(
        GameCategory.All,
        GameCategory.Quick,
        GameCategory.Romantic,
        GameCategory.Funny,
    )

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = palette.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            categories.forEach { category ->
                val selected = category == selectedCategory
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCategorySelected(category) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) palette.primarySoft else Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = categoryIcon(category),
                            contentDescription = null,
                            tint = if (selected) palette.primary else palette.mutedInk,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = categoryLabel(category),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) palette.ink else palette.mutedInk,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedGameCard(
    game: CoupleGameModel,
    palette: XendPalette,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            palette.primarySoft,
                            palette.surface,
                        ),
                    ),
                )
                .padding(14.dp),
        ) {
            SmallFloatingAccent(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 54.dp),
                tint = palette.primary,
                size = 8.dp,
            )
            SmallFloatingAccent(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 74.dp, top = 10.dp),
                tint = palette.primary,
                size = 6.dp,
            )
            SmallFloatingAccent(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 52.dp, bottom = 16.dp),
                tint = palette.primary,
                size = 6.dp,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.78f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Heroicons.Outline.Sparkles,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = game.badge ?: "Featured",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.primary,
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.ink,
                        )
                        Text(
                            text = game.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                            ),
                            color = palette.mutedInk,
                        )
                    }
                    Surface(
                        modifier = Modifier.clickable(onClick = onClick),
                        shape = RoundedCornerShape(999.dp),
                        color = palette.primary,
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Play Now",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.18f),
                            ) {
                                Box(
                                    modifier = Modifier.size(28.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Heroicons.Outline.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                FeaturedGameArt(
                    palette = palette,
                    modifier = Modifier.size(94.dp),
                )
            }
        }
    }
}

@Composable
private fun FeaturedGameArt(
    palette: XendPalette,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
                .size(62.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.72f),
        ) {}
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .size(68.dp),
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 3.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.Heart,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        SmallFloatingAccent(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 14.dp),
            tint = palette.primary,
            size = 8.dp,
        )
        SmallFloatingAccent(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 10.dp, top = 12.dp),
            tint = palette.primary,
            size = 7.dp,
        )
        SmallFloatingAccent(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 0.dp, top = 26.dp),
            tint = palette.primary,
            size = 6.dp,
        )
    }
}

@Composable
private fun GameCard(
    game: CoupleGameModel,
    palette: XendPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = rememberAccentBrush(game.accent, palette)
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.brush)
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.84f),
                ) {
                    Box(
                        modifier = Modifier.size(34.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = gameIcon(game),
                            contentDescription = null,
                            tint = colors.iconTint,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink,
                    )
                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                        ),
                        color = palette.mutedInk,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.iconTint.copy(alpha = 0.12f),
                ) {
                    Box(
                        modifier = Modifier.size(22.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Heroicons.Outline.ChevronRight,
                            contentDescription = null,
                            tint = colors.iconTint,
                            modifier = Modifier.size(11.dp),
                        )
                    }
                }
            }
        }
    }
}

private data class AccentColors(
    val brush: Brush,
    val iconTint: Color,
)

@Composable
private fun rememberAccentBrush(
    accent: GameAccent,
    palette: XendPalette,
): AccentColors {
    return when (accent) {
        GameAccent.Rose -> AccentColors(
            brush = Brush.linearGradient(
                colors = listOf(
                    palette.primarySoft,
                    palette.surface,
                ),
            ),
            iconTint = palette.primary,
        )
        GameAccent.Lavender -> AccentColors(
            brush = Brush.linearGradient(
                colors = listOf(
                    palette.lavenderSoft,
                    palette.surface,
                ),
            ),
            iconTint = palette.lavender,
        )
        GameAccent.Peach -> AccentColors(
            brush = Brush.linearGradient(
                colors = listOf(
                    palette.peachSoft,
                    palette.surface,
                ),
            ),
            iconTint = palette.orange,
        )
        GameAccent.Mint -> AccentColors(
            brush = Brush.linearGradient(
                colors = listOf(
                    palette.surfaceSoft,
                    palette.surface,
                ),
            ),
            iconTint = Color(0xFF2CA581),
        )
    }
}

private fun categoryLabel(category: GameCategory): String {
    return when (category) {
        GameCategory.All -> "All"
        GameCategory.Quick -> "Quick"
        GameCategory.Romantic -> "Romantic"
        GameCategory.Funny -> "Funny"
    }
}

private fun categoryIcon(category: GameCategory): ImageVector {
    return when (category) {
        GameCategory.All -> Heroicons.Outline.Sparkles
        GameCategory.Quick -> Heroicons.Solid.Fire
        GameCategory.Romantic -> Heroicons.Outline.Heart
        GameCategory.Funny -> Heroicons.Outline.FaceSmile
    }
}

private fun gameIcon(game: CoupleGameModel): ImageVector {
    return when (game.id) {
        "would-you-rather" -> Heroicons.Outline.ChatBubbleLeftRight
        "who-knows-me-better" -> Heroicons.Outline.Heart
        "truth-or-dare" -> Heroicons.Solid.Fire
        "love-quiz" -> Heroicons.Outline.Sparkles
        "this-or-that" -> Heroicons.Outline.ChatBubbleLeftRight
        "spin-the-wheel" -> Heroicons.Outline.Sparkles
        else -> Heroicons.Outline.Heart
    }
}
