package com.noztek.xend.feature.space.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ChatBubbleLeftRight
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.FaceSmile
import com.composables.icons.heroicons.outline.Gift
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.Sparkles
import com.composables.icons.heroicons.outline.Sun
import com.composables.icons.heroicons.outline.Trophy
import com.composables.icons.heroicons.solid.Fire
import com.composables.icons.heroicons.solid.Heart
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.model.SpaceHeroModel
import com.noztek.xend.feature.space.presentation.viewmodel.SpaceViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.couple
import xend.shared.generated.resources.couple_1
import xend.shared.generated.resources.pet

private data class MoodPreviewModel(
    val userMood: String,
    val partnerMood: String,
)

private data class PetPreviewModel(
    val petName: String,
    val petLevel: Int,
    val petStatus: String,
    val petEnergy: Float,
    val petLove: Float,
    val petNextReward: String,
)

private data class SpacePreviewContent(
    val mood: MoodPreviewModel,
    val pet: PetPreviewModel,
    val ritualPrompt: String,
)

private data class QuickActionUi(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color,
    val containerColor: Color,
    val onClick: () -> Unit,
)

private val previewContent = SpacePreviewContent(
    mood = MoodPreviewModel(
        userMood = "Tired",
        partnerMood = "Happy",
    ),
    pet = PetPreviewModel(
        petName = "Mochi",
        petLevel = 5,
        petStatus = "Happy",
        petEnergy = 0.66f,
        petLove = 0.78f,
        petNextReward = "Cozy Hat",
    ),
    ritualPrompt = "Share one thing you're looking forward to this week.",
)

private val mockHero = SpaceHeroModel(
    userName = "Johnny",
    partnerName = "Antonette",
    connectedDays = 128,
)

@Composable
fun SpaceScreen(
    onDailyRitualClick: () -> Unit = {},
    onGamesClick: () -> Unit = {},
    onChallengesClick: () -> Unit = {},
    onMessageClick: (String) -> Unit = {},
    viewModel: SpaceViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val hero = state.hero ?: mockHero
    val defaultSpace = state.defaultSpace
    val palette = rememberSpacePalette()
    val listState = rememberLazyListState()
    val conversationId = defaultSpace?.conversationId.orEmpty()
    val heroCollapseProgress by remember(listState) {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                else -> (listState.firstVisibleItemScrollOffset / 220f).coerceIn(0f, 1f)
            }
        }
    }
    val openChat = remember(conversationId, onMessageClick) {
        {
            if (conversationId.isNotBlank()) {
                onMessageClick(conversationId)
            }
        }
    }
    val quickActions = remember(openChat, palette, onChallengesClick) {
        listOf(
            QuickActionUi(
                title = "Chat",
                icon = Heroicons.Outline.ChatBubbleLeftRight,
                iconTint = palette.lavender,
                containerColor = palette.lavenderSoft,
                onClick = openChat,
            ),
            QuickActionUi(
                title = "Daily Ritual",
                icon = Heroicons.Outline.Heart,
                iconTint = palette.primary,
                containerColor = palette.primarySoft,
                onClick = onDailyRitualClick,
            ),
            QuickActionUi(
                title = "Games",
                icon = Heroicons.Outline.Sparkles,
                iconTint = palette.orange,
                containerColor = palette.orangeSoft,
                onClick = onGamesClick,
            ),
            QuickActionUi(
                title = "Challenges",
                icon = Heroicons.Outline.Trophy,
                iconTint = palette.lavender,
                containerColor = palette.lavenderSoft,
                onClick = onChallengesClick,
            ),
        )
    }

    LazyColumn(
        state = listState,
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
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HeroGreetingSection(
                hero = hero,
                palette = palette,
                collapseProgress = heroCollapseProgress,
            )
        }
        item {
            CoupleLevelCard(
                space = defaultSpace,
                palette = palette,
            )
        }
        item {
            QuickActionsSection(
                actions = quickActions,
                palette = palette,
            )
        }
        item {
            MoodCheckInCard(
                hero = hero,
                model = previewContent.mood,
                palette = palette,
            )
        }
        item {
            PetCard(
                model = previewContent.pet,
                palette = palette,
            )
        }
        item {
            TodaysRitualCard(
                ritualPrompt = previewContent.ritualPrompt,
                palette = palette,
                onClick = onDailyRitualClick,
            )
        }
    }
}

@Composable
private fun HeroGreetingSection(
    hero: SpaceHeroModel,
    palette: SpacePalette,
    collapseProgress: Float,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = collapseProgress,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "heroCollapse",
    )
    val imageSize = lerp(136.dp, 82.dp, animatedProgress)
    val titleSize = lerp(20.sp, 28.sp, animatedProgress)
    val titleLineHeight = lerp(28.sp, 36.sp, animatedProgress)
    val imageAlpha = 1f - (animatedProgress * 0.92f)
    val imageScale = 1f - (animatedProgress * 0.18f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            FloatingHeart(
                palette = palette,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 54.dp, top = 34.dp),
                size = 18.dp,
            )
            FloatingHeart(
                palette = palette,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 62.dp, top = 26.dp),
                size = 20.dp,
            )
            FloatingHeart(
                palette = palette,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 76.dp, top = 8.dp),
                size = 12.dp,
            )
            FloatingHeart(
                palette = palette,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 84.dp, top = 26.dp),
                size = 12.dp,
            )
            Surface(
                modifier = Modifier
                    .size(imageSize)
                    .graphicsLayer {
                        alpha = imageAlpha
                        scaleX = imageScale
                        scaleY = imageScale
                        translationY = -24f * animatedProgress
                    },
                shape = CircleShape,
                color = palette.surface,
                border = BorderStroke(4.dp, Color.White),
                shadowElevation = 3.dp,
            ) {
                Image(
                    painter = painterResource(Res.drawable.couple),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Hello, ${hero.userName} & ${hero.partnerName}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = titleSize,
                    lineHeight = titleLineHeight,
                ),
                color = palette.ink,
                textAlign = TextAlign.Center,
            )
            Icon(
                imageVector = Heroicons.Solid.Heart,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(16.dp),
            )
        }
        ConnectedDaysPill(
            connectedDays = hero.connectedDays,
            palette = palette,
        )
    }
}

@Composable
private fun PetCard(
    model: PetPreviewModel,
    palette: SpacePalette,
) {
    val shape = RoundedCornerShape(22.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Our Pet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.ink,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.pet),
                    contentDescription = model.petName,
                    modifier = Modifier
                        .size(100.dp),
                    contentScale = ContentScale.Fit,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = model.petName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink,
                    )
                    Text(
                        text = "${model.petStatus} • Lv. ${model.petLevel}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.primary,
                    )
                    Text(
                        text = "Feed, play, and grow your shared companion together.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.mutedInk,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PetStatChip(
                    title = "Energy",
                    icon = Heroicons.Outline.Sparkles,
                    value = model.petEnergy,
                    accent = palette.lavender,
                    background = palette.lavenderSoft,
                    modifier = Modifier.weight(1f),
                )
                PetStatChip(
                    title = "Love",
                    icon = Heroicons.Solid.Heart,
                    value = model.petLove,
                    accent = palette.primary,
                    background = palette.primarySoft,
                    modifier = Modifier.weight(1f),
                )
                PetRewardChip(
                    reward = model.petNextReward,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Visit Pet",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.primary,
                )
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = palette.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Heroicons.Outline.ChevronRight,
                            contentDescription = "Visit pet",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PetStatChip(
    title: String,
    icon: ImageVector,
    value: Float,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.wrapContentHeight(),
        color = background,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = accent,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.7f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(value.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent),
                )
            }
        }
    }
}

@Composable
private fun PetRewardChip(
    reward: String,
    palette: SpacePalette,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.wrapContentHeight(),
        color = palette.orangeSoft,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.Gift,
                    contentDescription = null,
                    tint = palette.orange,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Reward",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.orange,
                )
            }
            Text(
                text = reward,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ConnectedDaysPill(
    connectedDays: Int,
    palette: SpacePalette,
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
                            palette.primarySoft.copy(alpha = 0.95f * 2),
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
                text = "Connected",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$connectedDays days",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.primary,
            )
        }
    }
}

@Composable
private fun FloatingHeart(
    palette: SpacePalette,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp,
) {
    Icon(
        imageVector = Heroicons.Outline.Heart,
        contentDescription = null,
        tint = palette.primary.copy(alpha = 0.34f),
        modifier = modifier.size(size),
    )
}

@Composable
private fun CoupleLevelCard(
    space: RelationshipSpaceCardModel?,
    palette: SpacePalette,
) {
    val shape = RoundedCornerShape(22.dp)
    val progress = space?.let {
        (it.currentPoints.toFloat() / it.requiredPoints.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    } ?: 0f

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    color = palette.primarySoft,
                    shape = CircleShape,
                ) {
                    Box(
                        modifier = Modifier.size(62.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Heroicons.Outline.Heart,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Couple Level",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.ink,
                        )
                        Text(
                            text = space?.let { "Lv. ${it.currentLevel}" } ?: "Loading",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.primary,
                        )
                    }
                    Text(
                        text = space?.currentLevelName ?: "Bond progress",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = palette.mutedInk,
                    )
                    Text(
                        text = space?.let {
                            "${formatNumber(it.currentPoints)} / ${formatNumber(it.requiredPoints)}"
                        } ?: "-- / --",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(palette.progressTrack),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    palette.primaryBright,
                                    palette.primary,
                                ),
                            ),
                        ),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.Gift,
                    contentDescription = null,
                    tint = palette.mutedInk,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = space?.let { "Current bond: ${it.currentLevelName}" } ?: "Your bond details will appear here.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = palette.mutedInk,
                )
            }
        }
    }
}

@Composable
private fun MoodCheckInCard(
    hero: SpaceHeroModel,
    model: MoodPreviewModel,
    palette: SpacePalette,
) {
    val shape = RoundedCornerShape(22.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Mood Check-in",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.ink,
            )
            Text(
                text = "How are you both feeling?",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = palette.mutedInk,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MoodTile(
                    name = hero.userName,
                    mood = model.userMood,
                    iconTint = palette.lavender,
                    containerColor = palette.lavenderSoft,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
                MoodTile(
                    name = hero.partnerName,
                    mood = model.partnerMood,
                    iconTint = palette.primary,
                    containerColor = palette.peachSoft,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MoodTile(
    name: String,
    mood: String,
    iconTint: Color,
    containerColor: Color,
    palette: SpacePalette,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.FaceSmile,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                )
                Text(
                    text = mood,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = iconTint,
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    actions: List<QuickActionUi>,
    palette: SpacePalette,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        actions.forEach { action ->
            QuickActionCard(
                action = action,
                palette = palette,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickActionUi,
    palette: SpacePalette,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)

    Card(
        modifier = modifier
            .clickable(onClick = action.onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = action.iconTint,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TodaysRitualCard(
    ritualPrompt: String,
    palette: SpacePalette,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(palette.orangeSoft, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.Sun,
                    contentDescription = null,
                    tint = palette.orange,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Today's Ritual",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )
                Text(
                    text = ritualPrompt,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = palette.ink,
                )
            }
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onClick),
                shape = CircleShape,
                color = palette.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.ChevronRight,
                        contentDescription = "Open ritual",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

private fun formatNumber(value: Int): String {
    return value
        .toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}

private data class SpacePalette(
    val background: Color,
    val backgroundGlow: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val surfaceRaised: Color,
    val ink: Color,
    val mutedInk: Color,
    val softInk: Color,
    val warmInk: Color,
    val outline: Color,
    val primary: Color,
    val primaryBright: Color,
    val primarySoft: Color,
    val heroText: Color,
    val progressTrack: Color,
    val progressFill: Color,
    val lavender: Color,
    val lavenderSoft: Color,
    val peachSoft: Color,
    val orange: Color,
    val orangeSoft: Color,
)

@Composable
private fun rememberSpacePalette(): SpacePalette {
    return remember {
        SpacePalette(
            background = Color(0xFFFFFAF7),
            backgroundGlow = Color(0xFFFFF3F6),
            surface = Color(0xFFFFFFFF),
            surfaceSoft = Color(0xFFFFF5F7),
            surfaceRaised = Color(0xFFFFF7F8),
            ink = Color(0xFF171725),
            mutedInk = Color(0xFF7D8091),
            softInk = Color(0xFF8E90A1),
            warmInk = Color(0xFFF08CA3),
            outline = Color(0xFFF6EBEE),
            primary = Color(0xFFF56C91),
            primaryBright = Color(0xFFFF7FA0),
            primarySoft = Color(0xFFFFEEF4),
            heroText = Color.White,
            progressTrack = Color(0xFFFBE1E8),
            progressFill = Color(0xFFF56C91),
            lavender = Color(0xFF8D72F7),
            lavenderSoft = Color(0xFFF3EEFF),
            peachSoft = Color(0xFFFFF1ED),
            orange = Color(0xFFF5A33D),
            orangeSoft = Color(0xFFFFF5E7),
        )
    }
}
