package com.noztek.xend.feature.dailycheckin.presentation.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.Fire
import com.composables.icons.heroicons.outline.Gift
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.Sparkles
import com.composables.icons.heroicons.outline.Trophy
import com.composables.icons.heroicons.outline.XMark
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
import com.noztek.xend.feature.dailycheckin.presentation.state.DailyCheckInCelebrationDialogModel
import com.noztek.xend.feature.dailycheckin.presentation.viewmodel.DailyCheckInViewModel
import org.koin.compose.koinInject

@Composable
fun DailyCheckInScreen(
    onHistoryClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
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
                    onCloseClick = onCloseClick,
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

    state.celebrationDialog?.let { dialog ->
        DailyCheckInCelebrationDialog(
            dialog = dialog,
            palette = palette,
            onDismiss = viewModel::dismissCelebrationDialog,
        )
    }
}

@Composable
private fun DailyCheckInContent(
    overview: DailyCheckInOverviewModel,
    isSubmitting: Boolean,
    message: String?,
    palette: XendPalette,
    onCloseClick: () -> Unit,
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
                onCloseClick = onCloseClick,
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
private fun DailyCheckInCelebrationDialog(
    dialog: DailyCheckInCelebrationDialogModel,
    palette: XendPalette,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            color = Color(0xFFFFFBF7),
            shadowElevation = 12.dp,
        ) {
            Box(
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFBF7),
                            palette.primarySoft.copy(alpha = 0.18f),
                            Color(0xFFFFFBF7),
                        ),
                    ),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CheckInConfettiBurst(palette = palette)
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = palette.primary,
                            shadowElevation = 3.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Heroicons.Solid.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(27.dp),
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Check-in Complete!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 23.sp,
                                lineHeight = 28.sp,
                            ),
                            color = Color(0xFF302325),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "You both showed up today.\nSmall moments build strong connections.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            ),
                            color = Color(0xFF756A6B),
                            textAlign = TextAlign.Center,
                        )
                    }

                    StreakSummaryStrip(
                        streakDays = dialog.streakDays,
                        palette = palette,
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Rewards Earned",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF302325),
                        )
                        RewardEarnedRow(
                            icon = Heroicons.Outline.Heart,
                            iconTint = palette.primary,
                            iconBackground = Color(0xFFFFDDE4),
                            title = "Daily Check-in",
                            subtitle = "For showing up together",
                            points = dialog.dailyRewardPoints,
                            palette = palette,
                        )

                        if (dialog.milestoneDays != null && dialog.milestoneBonusPoints != null) {
                            HorizontalDivider(color = Color(0xFFECDCDC))
                            RewardEarnedRow(
                                icon = Heroicons.Outline.Gift,
                                iconTint = Color(0xFFFF9E21),
                                iconBackground = Color(0xFFFFEBC8),
                                title = "Streak Bonus (${dialog.milestoneDays} Days)",
                                subtitle = "Milestone reward",
                                points = dialog.milestoneBonusPoints,
                                palette = palette,
                            )
                        }
                    }

                    TotalEarnedBanner(
                        points = dialog.totalRewardPoints,
                        palette = palette,
                    )

                    AppButton(
                        text = "Continue",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = palette.primary,
                        contentColor = Color.White,
                    )
                }

                Icon(
                    imageVector = Heroicons.Outline.XMark,
                    contentDescription = "Close",
                    tint = Color(0xFF3C3031),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(21.dp)
                        .clickable(onClick = onDismiss),
                )
            }
        }
    }
}

@Composable
private fun CheckInConfettiBurst(
    palette: XendPalette,
) {
    val transition = rememberInfiniteTransition(label = "checkInConfetti")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
        ),
        label = "checkInConfettiDrift",
    )
    val pieces = listOf(
        ConfettiPiece(0.28f, 0.27f, 0.00f, 0xFFFF8FA3, 10f, 32f),
        ConfettiPiece(0.35f, 0.18f, 0.23f, 0xFFFFC078, 5f, -20f),
        ConfettiPiece(0.42f, 0.62f, 0.38f, 0xFFFFB7C5, 4f, 18f),
        ConfettiPiece(0.58f, 0.24f, 0.11f, 0xFFFFC078, 5f, 26f),
        ConfettiPiece(0.66f, 0.54f, 0.31f, 0xFFFF8FA3, 12f, -28f),
        ConfettiPiece(0.74f, 0.36f, 0.48f, 0xFFFFC078, 5f, 16f),
        ConfettiPiece(0.31f, 0.54f, 0.63f, 0xFFFFC9D2, 4f, 8f),
        ConfettiPiece(0.70f, 0.66f, 0.78f, 0xFFFFC9D2, 4f, -14f),
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        pieces.forEachIndexed { index, piece ->
            val phase = (drift + piece.delay) % 1f
            val center = Offset(
                x = size.width * piece.x,
                y = size.height * piece.y + ((phase - 0.5f) * 14.dp.toPx()),
            )
            val color = Color(piece.color).copy(alpha = 0.35f + (1f - phase) * 0.45f)

            if (index % 3 == 0) {
                drawCircle(
                    color = color,
                    radius = piece.size.dp.toPx() / 2f,
                    center = center,
                )
            } else {
                rotate(degrees = piece.rotation + phase * 180f, pivot = center) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(
                            x = center.x - 3.dp.toPx(),
                            y = center.y - piece.size.dp.toPx(),
                        ),
                        size = Size(6.dp.toPx(), (piece.size * 1.7f).dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    )
                }
            }
        }

        drawCircle(
            color = palette.primary.copy(alpha = 0.16f),
            radius = 36.dp.toPx(),
            center = Offset(size.width / 2f, size.height / 2f),
        )
    }
}

@Composable
private fun StreakSummaryStrip(
    streakDays: Int,
    palette: XendPalette,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = Color(0xFFFFF1E9),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Heroicons.Outline.Fire,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(26.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "$streakDays Day Streak",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF302325),
                )
                Text(
                    text = "Keep it going!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF756A6B),
                )
            }
            Spacer(
                modifier = Modifier
                    .height(34.dp)
                    .width(1.dp)
                    .background(Color(0xFFE7CFC9)),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.CalendarDays,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp),
                )
                Column {
                    Text(
                        text = "$streakDays days",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.primary,
                    )
                    Text(
                        text = "in a row",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF756A6B),
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardEarnedRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    points: Int,
    palette: XendPalette,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = iconBackground,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF302325),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF756A6B),
            )
        }
        Text(
            text = "+$points BP",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = palette.primary,
        )
    }
}

@Composable
private fun TotalEarnedBanner(
    points: Int,
    palette: XendPalette,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = palette.primarySoft.copy(alpha = 0.66f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Total Earned",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF302325),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "+$points BP",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                    ),
                    color = palette.primary,
                )
                Icon(
                    imageVector = Heroicons.Outline.Sparkles,
                    contentDescription = null,
                    tint = palette.primary.copy(alpha = 0.46f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val delay: Float,
    val color: Long,
    val size: Float,
    val rotation: Float,
)

@Composable
private fun HeaderSection(
    palette: XendPalette,
    onCloseClick: () -> Unit,
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

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .clickable(onClick = onCloseClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Heroicons.Outline.XMark,
                contentDescription = "Close daily check-in",
                tint = palette.ink,
                modifier = Modifier.size(24.dp),
            )
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
