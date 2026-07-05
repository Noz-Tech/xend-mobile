package com.noztek.xend.feature.challenges.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.HorizontalDivider
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
import com.composables.icons.heroicons.outline.ChatBubbleLeftRight
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Gift
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.Sparkles
import com.composables.icons.heroicons.outline.Sun
import com.noztek.xend.core.ui.theme.XendPalette
import com.noztek.xend.core.ui.theme.XendTheme
import com.noztek.xend.feature.challenges.domain.model.ChallengeAccent
import com.noztek.xend.feature.challenges.domain.model.ChallengeActionStyle
import com.noztek.xend.feature.challenges.domain.model.ChallengeAudience
import com.noztek.xend.feature.challenges.domain.model.ChallengeCategory
import com.noztek.xend.feature.challenges.domain.model.ChallengeIdeaModel
import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel
import com.noztek.xend.feature.challenges.domain.model.DailyChallengeModel
import com.noztek.xend.feature.challenges.presentation.viewmodel.ChallengesViewModel
import org.koin.compose.koinInject

@Composable
fun ChallengesScreen(
    onRewardClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onPrimaryActionClick: (String) -> Unit = {},
    onAddActionClick: (String) -> Unit = {},
    onProgressClick: () -> Unit = {},
    viewModel: ChallengesViewModel = koinInject(),
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
                ChallengesContent(
                    overview = requireNotNull(state.overview),
                    selectedAudience = state.selectedAudience,
                    selectedCategory = state.selectedCategory,
                    palette = palette,
                    onRewardClick = onRewardClick,
                    onCalendarClick = onCalendarClick,
                    onAudienceSelected = viewModel::onAudienceSelected,
                    onCategorySelected = viewModel::onCategorySelected,
                    onPrimaryActionClick = onPrimaryActionClick,
                    onAddActionClick = onAddActionClick,
                    onProgressClick = onProgressClick,
                )
            }

            else -> {
                Text(
                    text = state.message ?: "Unable to load challenges.",
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
private fun ChallengesContent(
    overview: ChallengesOverviewModel,
    selectedAudience: ChallengeAudience,
    selectedCategory: ChallengeCategory,
    palette: XendPalette,
    onRewardClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAudienceSelected: (ChallengeAudience) -> Unit,
    onCategorySelected: (ChallengeCategory) -> Unit,
    onPrimaryActionClick: (String) -> Unit,
    onAddActionClick: (String) -> Unit,
    onProgressClick: () -> Unit,
) {
    val ideas = overview.ideas.filter { idea ->
        (selectedAudience == idea.audience) &&
            (selectedCategory == ChallengeCategory.All || selectedCategory == idea.category)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ChallengesHeader(
                palette = palette,
                onRewardClick = onRewardClick,
            )
        }
        item {
            AudienceTabs(
                selectedAudience = selectedAudience,
                palette = palette,
                onAudienceSelected = onAudienceSelected,
            )
        }
        item {
            HeroChallengeCard(palette = palette)
        }
        item {
            DailyChallengeHeader(
                palette = palette,
                onCalendarClick = onCalendarClick,
            )
        }
        item {
            DailyChallengeCard(
                challenge = overview.dailyChallenge,
                palette = palette,
            )
        }
        item {
            ChallengeIdeasHeader(palette = palette)
        }
        item {
            CategoryChips(
                selectedCategory = selectedCategory,
                palette = palette,
                onCategorySelected = onCategorySelected,
            )
        }
        items(ideas) { item ->
            ChallengeIdeaCard(
                item = item,
                palette = palette,
                onPrimaryActionClick = onPrimaryActionClick,
                onAddActionClick = onAddActionClick,
            )
        }
        item {
            ConsistencyCard(
                message = overview.progressMessage,
                palette = palette,
                onProgressClick = onProgressClick,
            )
        }
    }
}

@Composable
private fun ChallengesHeader(
    palette: XendPalette,
    onRewardClick: () -> Unit,
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
                    text = "Challenges",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                    ),
                    color = palette.ink,
                )
            }
            Text(
                text = "Little things, big love.",
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
                .clickable(onClick = onRewardClick),
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
                    imageVector = Heroicons.Outline.Gift,
                    contentDescription = "Rewards",
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun AudienceTabs(
    selectedAudience: ChallengeAudience,
    palette: XendPalette,
    onAudienceSelected: (ChallengeAudience) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(35.dp),
        color = palette.surface,
        shadowElevation = 0.5.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(ChallengeAudience.ForYou, ChallengeAudience.ForThem).forEach { audience ->
                val selected = audience == selectedAudience
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAudienceSelected(audience) },
                    shape = RoundedCornerShape(35.dp),
                    color = if (selected) palette.primary else Color.Transparent,
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (audience == ChallengeAudience.ForYou) "For You" else "For Them",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) palette.primarySoft else palette.ink,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroChallengeCard(
    palette: XendPalette,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            palette.primarySoft,
                            palette.primarySoft.copy(alpha = 0.6f),
                        ),
                    ),
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Love is in\nthe little things",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )
                Text(
                    text = "Complete challenges together and grow closer every day.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    ),
                    color = palette.ink,
                )
            }
            HeroArt(
                palette = palette,
                modifier = Modifier.size(82.dp),
            )
        }
    }
}

@Composable
private fun HeroArt(
    palette: XendPalette,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        FloatingHeart(
            tint = palette.primary,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 10.dp),
            size = 8.dp,
        )
        FloatingHeart(
            tint = palette.primaryBright,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 6.dp, top = 12.dp),
            size = 8.dp,
        )
        FloatingHeart(
            tint = palette.primary,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 0.dp, top = 14.dp),
            size = 6.dp,
        )
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 2.dp,
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
    }
}

@Composable
private fun DailyChallengeHeader(
    palette: XendPalette,
    onCalendarClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Daily Challenge",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.ink,
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = palette.primarySoft,
            ) {
                Text(
                    text = "New",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.primary,
                )
            }
        }
        Text(
            text = "View calendar",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.primary,
            modifier = Modifier.clickable(onClick = onCalendarClick),
        )
    }
}

@Composable
private fun DailyChallengeCard(
    challenge: DailyChallengeModel,
    palette: XendPalette,
) {
    val progress = (challenge.completedCount.toFloat() / challenge.totalCount.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AccentCircle(
                    icon = Heroicons.Outline.ChatBubbleLeftRight,
                    tint = palette.primary,
                    background = palette.primarySoft,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink,
                    )
                    Text(
                        text = challenge.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        ),
                        color = palette.mutedInk,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = palette.primarySoft,
                    ),
                ) {
                    Text(
                        text = "+${challenge.bondPoints} BP",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.primary,
                    )
                }
            }

            HorizontalDivider(color = palette.outline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "${challenge.completedCount}/${challenge.totalCount} completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.ink,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            color = palette.progressTrack,
                            shape = RoundedCornerShape(999.dp),
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .background(
                                color = palette.primary,
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                }
                Text(
                    text = "${challenge.hoursLeft}h left",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.mutedInk,
                )
            }
        }
    }
}

@Composable
private fun ChallengeIdeasHeader(
    palette: XendPalette,
) {
    Text(
        text = "Challenge Ideas",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = palette.ink,
    )
}

@Composable
private fun CategoryChips(
    selectedCategory: ChallengeCategory,
    palette: XendPalette,
    onCategorySelected: (ChallengeCategory) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            ChallengeCategory.All,
            ChallengeCategory.Romantic,
            ChallengeCategory.Fun,
            ChallengeCategory.Supportive,
        ).forEach { category ->
            val selected = category == selectedCategory
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) palette.primarySoft else palette.surface,
            ) {
                Text(
                    text = challengeCategoryLabel(category),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selected) palette.primary else palette.mutedInk,
                )
            }
        }
    }
}

@Composable
private fun ChallengeIdeaCard(
    item: ChallengeIdeaModel,
    palette: XendPalette,
    onPrimaryActionClick: (String) -> Unit,
    onAddActionClick: (String) -> Unit,
) {
    val accent = challengeAccent(item.accent, palette)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AccentCircle(
                icon = accent.icon,
                tint = accent.tint,
                background = accent.background,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    ),
                    color = palette.mutedInk,
                )
                Text(
                    text = "❤ +${item.bondPoints} BP",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primary,
                )
            }
            when (item.actionStyle) {
                ChallengeActionStyle.Primary -> {
                    Surface(
                        modifier = Modifier.clickable { onPrimaryActionClick(item.id) },
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = palette.primarySoft,
                        ),
                    ) {
                        Text(
                            text = "Send",
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.primary,
                        )
                    }
                }

                ChallengeActionStyle.Add -> {
                    Surface(
                        modifier = Modifier.clickable { onAddActionClick(item.id) },
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.5.dp,
                            color = palette.primarySoft,
                        ),
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Heroicons.Outline.Plus,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsistencyCard(
    message: String,
    palette: XendPalette,
    onProgressClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            palette.primarySoft,
                            palette.surface,
                        ),
                    ),
                )
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AccentCircle(
                icon = Heroicons.Outline.Gift,
                tint = palette.primary,
                background = Color.White.copy(alpha = 0.7f),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Consistency is key!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    ),
                    color = palette.ink,
                )
            }
            Surface(
                modifier = Modifier.clickable(onClick = onProgressClick),
                shape = RoundedCornerShape(16.dp),
                color = palette.primary,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "See Progress",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccentCircle(
    icon: ImageVector,
    tint: Color,
    background: Color,
) {
    Surface(
        shape = CircleShape,
        color = background,
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private data class ChallengeAccentUi(
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
)

@Composable
private fun challengeAccent(
    accent: ChallengeAccent,
    palette: XendPalette,
): ChallengeAccentUi {
    return when (accent) {
        ChallengeAccent.Sunrise -> ChallengeAccentUi(
            icon = Heroicons.Outline.Sun,
            tint = palette.orange,
            background = palette.orangeSoft,
        )
        ChallengeAccent.Lavender -> ChallengeAccentUi(
            icon = Heroicons.Outline.ChatBubbleLeftRight,
            tint = palette.lavender,
            background = palette.lavenderSoft,
        )
        ChallengeAccent.Mint -> ChallengeAccentUi(
            icon = Heroicons.Outline.Heart,
            tint = Color(0xFF53B992),
            background = Color(0xFFE6F7F0),
        )
        ChallengeAccent.Rose -> ChallengeAccentUi(
            icon = Heroicons.Outline.Gift,
            tint = palette.primary,
            background = palette.primarySoft,
        )
    }
}

@Composable
private fun FloatingHeart(
    tint: Color,
    modifier: Modifier = Modifier,
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
                modifier = Modifier.size(size * 0.56f),
            )
        }
    }
}

private fun challengeCategoryLabel(category: ChallengeCategory): String {
    return when (category) {
        ChallengeCategory.All -> "All"
        ChallengeCategory.Romantic -> "Romantic"
        ChallengeCategory.Fun -> "Fun"
        ChallengeCategory.Supportive -> "Supportive"
    }
}
