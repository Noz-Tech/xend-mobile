package com.noztek.xend.feature.dailycheckin.presentation.screen

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Fire
import com.composables.icons.heroicons.outline.Gift
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.Trophy
import com.composables.icons.heroicons.solid.Check
import com.composables.icons.heroicons.solid.CheckCircle
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.theme.XendPalette
import com.noztek.xend.core.ui.theme.XendTheme
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInMemberModel
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInMilestoneModel
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInMilestoneStatus
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInMoodTone
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInOverviewModel
import com.noztek.xend.feature.dailycheckin.presentation.viewmodel.DailyCheckInViewModel
import org.koin.compose.koinInject

@Composable
fun DailyCheckInScreen(
    onHistoryClick: () -> Unit = {},
    onCheckedInClick: () -> Unit = {},
    viewModel: DailyCheckInViewModel = koinInject(),
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
                DailyCheckInContent(
                    overview = requireNotNull(state.overview),
                    isSubmitting = state.isSubmitting,
                    message = state.message,
                    palette = palette,
                    onHistoryClick = onHistoryClick,
                    onSubmitClick = { viewModel.submit(onSuccess = onCheckedInClick) },
                )
            }

            else -> {
                Text(
                    text = state.message ?: "Unable to load daily check-in.",
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
private fun DailyCheckInContent(
    overview: DailyCheckInOverviewModel,
    isSubmitting: Boolean,
    message: String?,
    palette: XendPalette,
    onHistoryClick: () -> Unit,
    onSubmitClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            HeaderSection(
                palette = palette,
                onHistoryClick = onHistoryClick,
            )
        }
        item {
            StreakHeroCard(
                overview = overview,
                palette = palette,
            )
        }
        item {
            TodayCheckInCard(
                overview = overview,
                palette = palette,
            )
        }
        item {
            MilestonesCard(
                overview = overview,
                palette = palette,
            )
        }
        item {
            AppButton(
                text = if (overview.myCheckedIn) "Checked In Today" else "Check In Today",
                onClick = onSubmitClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !overview.myCheckedIn && !isSubmitting,
                isLoading = isSubmitting,
                containerColor = palette.primary,
                contentColor = Color.White,
            )
        }
        if (!message.isNullOrBlank()) {
            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = palette.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        item {
            Text(
                text = "View Check-In History",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = palette.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHistoryClick)
                    .padding(bottom = 10.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun HeaderSection(
    palette: XendPalette,
    onHistoryClick: () -> Unit,
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
                    text = "Daily Check-In",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 23.sp,
                    ),
                    color = palette.ink,
                )
                HeartCluster(palette = palette)
            }
            Text(
                text = "Show up together, every day.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
                color = palette.mutedInk,
            )
        }

        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onHistoryClick),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
            color = Color.White.copy(alpha = 0.82f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = palette.primarySoft,
            ),
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.CalendarDays,
                    contentDescription = "Check-in history",
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
private fun StreakHeroCard(
    overview: DailyCheckInOverviewModel,
    palette: XendPalette,
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            palette.surface,
                            palette.primarySoft.copy(alpha = 0.72f),
                        ),
                    ),
                )
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = palette.primarySoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.Fire,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(27.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "${overview.streakDays}-Day Streak",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.primary,
                )
                Text(
                    text = overview.streakSummary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = palette.mutedInk,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Heroicons.Outline.Heart,
                    contentDescription = null,
                    tint = palette.primary.copy(alpha = 0.38f),
                    modifier = Modifier.size(18.dp),
                )
                Icon(
                    imageVector = Heroicons.Outline.Heart,
                    contentDescription = null,
                    tint = palette.primaryBright.copy(alpha = 0.58f),
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Composable
private fun TodayCheckInCard(
    overview: DailyCheckInOverviewModel,
    palette: XendPalette,
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = palette.primarySoft,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Heroicons.Outline.Heart,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                    Text(
                        text = "Today • ${overview.dateLabel}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink,
                    )
                }
                StatusChip(
                    text = overview.pendingCheckInLabel(),
                    palette = palette,
                    positive = overview.bothCheckedIn,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MemberColumn(
                    member = overview.user,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
                CenterDividerHeart(
                    palette = palette,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                MemberColumn(
                    member = overview.partner,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
            }

            HorizontalDivider(color = palette.outline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        color = palette.primarySoft,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Heroicons.Outline.Gift,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        Text(
                            text = "Today's Reward",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.ink,
                        )
                        Text(
                            text = "+${overview.rewardPoints} BP",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.primary,
                        )
                        Text(
                            text = "You earn points when you both check in.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                            ),
                            color = palette.mutedInk,
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                StatusChip(
                    text = if (overview.bothCheckedIn) "Both checked in" else "Pending",
                    palette = palette,
                    positive = overview.bothCheckedIn,
                )
            }
        }
    }
}

private fun DailyCheckInOverviewModel.pendingCheckInLabel(): String {
    return when {
        bothCheckedIn -> "Both checked in"
        user.checkedIn && !partner.checkedIn -> "Waiting for ${partner.title}"
        !user.checkedIn && partner.checkedIn -> "Waiting for you"
        else -> "Waiting for both"
    }
}

@Composable
private fun MemberColumn(
    member: DailyCheckInMemberModel,
    palette: XendPalette,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = member.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = palette.ink,
        )
        Box(
            contentAlignment = Alignment.BottomEnd,
        ) {
            Surface(
                modifier = Modifier.size(74.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = palette.surfaceSoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = member.initials,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink,
                    )
                }
            }
        }
        MoodPill(
            moodLabel = member.moodLabel,
            tone = member.moodTone,
            palette = palette,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Heroicons.Solid.CheckCircle,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = if (member.checkedIn) "Checked in" else "Not yet checked in",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = palette.primary,
            )
        }
    }
}

@Composable
private fun MoodPill(
    moodLabel: String,
    tone: DailyCheckInMoodTone,
    palette: XendPalette,
) {
    val containerColor = when (tone) {
        DailyCheckInMoodTone.Calm -> Color(0xFFE5F6EC)
        DailyCheckInMoodTone.Happy -> Color(0xFFFFF1C9)
    }
    val accentColor = when (tone) {
        DailyCheckInMoodTone.Calm -> Color(0xFF2E8B57)
        DailyCheckInMoodTone.Happy -> Color(0xFFB97800)
    }
    val symbol = when (tone) {
        DailyCheckInMoodTone.Calm -> "C"
        DailyCheckInMoodTone.Happy -> "H"
    }

    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Surface(
                modifier = Modifier.size(20.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Color.White.copy(alpha = 0.72f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = symbol,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = accentColor,
                    )
                }
            }
            Text(
                text = moodLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor,
            )
        }
    }
}

@Composable
private fun CenterDividerHeart(
    palette: XendPalette,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(40.dp)
            .height(132.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .width(40.dp)
                .height(132.dp),
        ) {
            drawLine(
                color = palette.outline,
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        Surface(
            modifier = Modifier.size(34.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = palette.primary,
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.Heart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun MilestonesCard(
    overview: DailyCheckInOverviewModel,
    palette: XendPalette,
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.Trophy,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Streak Milestones",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )
            }

            MilestoneTrack(
                milestones = overview.milestones,
                palette = palette,
            )

            overview.milestones.firstOrNull { it.status == DailyCheckInMilestoneStatus.Current }?.let { unlocked ->
                HighlightMilestoneCard(
                    milestone = unlocked,
                    palette = palette,
                )
            }

            NextMilestoneCard(
                currentDays = overview.streakDays,
                nextMilestoneDays = overview.nextMilestoneDays,
                remainingDays = overview.nextMilestoneRemainingDays,
                palette = palette,
            )
        }
    }
}

@Composable
private fun MilestoneTrack(
    milestones: List<DailyCheckInMilestoneModel>,
    palette: XendPalette,
) {
    val circleSize = 36.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        milestones.forEachIndexed { index, milestone ->
            Column(
                modifier = Modifier.width(64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Surface(
                    modifier = Modifier.size(circleSize),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = when (milestone.status) {
                        DailyCheckInMilestoneStatus.Reached -> palette.primarySoft
                        DailyCheckInMilestoneStatus.Current -> palette.primary
                        DailyCheckInMilestoneStatus.Locked -> palette.surfaceSoft
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = when (milestone.status) {
                            DailyCheckInMilestoneStatus.Reached -> palette.primary.copy(alpha = 0.28f)
                            DailyCheckInMilestoneStatus.Current -> palette.primary
                            DailyCheckInMilestoneStatus.Locked -> palette.outline
                        },
                    ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (milestone.status) {
                                DailyCheckInMilestoneStatus.Reached -> Heroicons.Outline.Heart
                                DailyCheckInMilestoneStatus.Current -> Heroicons.Solid.Check
                                DailyCheckInMilestoneStatus.Locked -> Heroicons.Outline.Gift
                            },
                            contentDescription = null,
                            tint = when (milestone.status) {
                                DailyCheckInMilestoneStatus.Reached -> palette.primary
                                DailyCheckInMilestoneStatus.Current -> Color.White
                                DailyCheckInMilestoneStatus.Locked -> palette.softInk
                            },
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Text(
                    text = "${milestone.days} Days",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = when (milestone.status) {
                        DailyCheckInMilestoneStatus.Current -> palette.primary
                        else -> palette.ink
                    },
                    textAlign = TextAlign.Center,
                )
            }

            if (index < milestones.lastIndex) {
                val nextMilestone = milestones[index + 1]
                MilestoneConnector(
                    locked = nextMilestone.status == DailyCheckInMilestoneStatus.Locked,
                    palette = palette,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 17.dp),
                )
            }
        }
    }
}

@Composable
private fun MilestoneConnector(
    locked: Boolean,
    palette: XendPalette,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .height(4.dp)
            .padding(horizontal = 4.dp),
    ) {
        val strokeWidth = 2.dp.toPx()
        val dashWidth = 6.dp.toPx()
        val gapWidth = 5.dp.toPx()
        val centerY = size.height / 2f

        if (locked) {
            var currentX = 0f
            while (currentX < size.width) {
                val dashEnd = (currentX + dashWidth).coerceAtMost(size.width)
                drawLine(
                    color = palette.outline.copy(alpha = 0.92f),
                    start = Offset(currentX, centerY),
                    end = Offset(dashEnd, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                currentX += dashWidth + gapWidth
            }
        } else {
            drawLine(
                color = palette.primary.copy(alpha = 0.78f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun HighlightMilestoneCard(
    milestone: DailyCheckInMilestoneModel,
    palette: XendPalette,
) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = palette.primarySoft.copy(alpha = 0.84f),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.primarySoft),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.55f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.Gift,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = "${milestone.days}-Day Milestone Unlocked",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )
                Text(
                    text = "+${milestone.bonusPoints} BP bonus",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primary,
                )
            }
            Surface(
                modifier = Modifier.size(34.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = palette.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.Heart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun NextMilestoneCard(
    currentDays: Int,
    nextMilestoneDays: Int,
    remainingDays: Int,
    palette: XendPalette,
) {
    val progress = (currentDays.toFloat() / nextMilestoneDays.toFloat()).coerceIn(0f, 1f)

    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = palette.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = palette.primarySoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.CalendarDays,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Next milestone: $nextMilestoneDays days",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink,
                )
                Text(
                    text = "$remainingDays days to go",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.mutedInk,
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = palette.primary,
                    trackColor = palette.outline,
                )
            }
            Text(
                text = "$currentDays / $nextMilestoneDays",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.ink,
            )
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    palette: XendPalette,
    positive: Boolean,
) {
    val containerColor = if (positive) Color(0xFFE6F6EC) else palette.primarySoft
    val contentColor = if (positive) Color(0xFF2E8B57) else palette.primary

    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Heroicons.Solid.CheckCircle,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor,
            )
        }
    }
}
