package com.noztek.xend.feature.dailyritual.presentation.screen

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.Camera
import com.composables.icons.heroicons.outline.ChatBubbleLeftRight
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.FaceSmile
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.Sparkles
import com.composables.icons.heroicons.outline.Sun
import com.composables.icons.heroicons.solid.Check
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.core.ui.media.rememberImagePickerLauncher
import com.noztek.xend.core.ui.theme.XendPalette
import com.noztek.xend.core.ui.theme.XendTheme
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualHistoryItemModel
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualOverviewModel
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualTodayModel
import com.noztek.xend.feature.dailyritual.domain.model.RitualItemKind
import com.noztek.xend.feature.dailyritual.presentation.state.DailyRitualUiState
import com.noztek.xend.feature.dailyritual.presentation.viewmodel.DailyRitualViewModel
import androidx.compose.foundation.text.KeyboardOptions
import org.koin.compose.koinInject

@Composable
fun DailyRitualScreen(
    onCalendarClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onAddCustomRitualClick: () -> Unit = {},
    viewModel: DailyRitualViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val palette = XendTheme.palette
    val snackbarHostState = remember { SnackbarHostState() }
    val imagePickerLauncher = rememberImagePickerLauncher(
        onPicked = { image ->
            state.overview?.todayRitual?.let { ritual ->
                viewModel.submitTodayRitualImage(
                    assignmentId = ritual.assignmentId,
                    image = image,
                )
            }
        },
        onUnavailable = viewModel::showMessage,
    )

    LaunchedEffect(state.message, state.overview) {
        val message = state.message
        if (message.isNullOrBlank() || state.overview == null) return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message)
        viewModel.onMessageConsumed()
    }

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
                RitualContent(
                    state = state,
                    overview = requireNotNull(state.overview),
                    palette = palette,
                    onCalendarClick = onCalendarClick,
                    onEditClick = onEditClick,
                    onAddCustomRitualClick = onAddCustomRitualClick,
                    onSubmitRitual = viewModel::submitTodayRitual,
                    onOpenTextResponse = viewModel::openResponseComposer,
                    onOpenImagePicker = imagePickerLauncher,
                )
            }

            else -> {
                Text(
                    text = state.message ?: "Unable to load rituals.",
                    color = palette.mutedInk,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 18.dp),
        )
    }

    val todayRitual = state.overview?.todayRitual
    if (state.isResponseComposerVisible && todayRitual != null) {
        DailyRitualResponseDialog(
            isSubmitting = state.isSubmitting,
            draft = state.responseDraft,
            onDraftChange = viewModel::onResponseDraftChanged,
            onDismiss = viewModel::dismissResponseComposer,
            onSubmit = {
                viewModel.submitTodayRitual(
                    assignmentId = todayRitual.assignmentId,
                    textResponse = state.responseDraft,
                )
            },
        )
    }
}

@Composable
private fun RitualContent(
    state: DailyRitualUiState,
    overview: DailyRitualOverviewModel,
    palette: XendPalette,
    onCalendarClick: () -> Unit,
    onEditClick: () -> Unit,
    onAddCustomRitualClick: () -> Unit,
    onSubmitRitual: (String, String?) -> Unit,
    onOpenTextResponse: () -> Unit,
    onOpenImagePicker: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeaderSection(
                palette = palette,
                onCalendarClick = onCalendarClick,
            )
        }
        item {
            ProgressSummaryCard(
                overview = overview,
                palette = palette,
            )
        }
        item {
            TodayRitualCard(
                ritual = overview.todayRitual,
                palette = palette,
                isSubmitting = state.isSubmitting,
                onSubmitRitual = onSubmitRitual,
                onOpenTextResponse = onOpenTextResponse,
                onOpenImagePicker = onOpenImagePicker,
            )
        }
        item {
            SectionHeader(
                title = "Ritual History",
                palette = palette,
            )
        }
        item {
            RitualChecklistCard(
                rituals = overview.history,
                palette = palette,
                emptyText = "No ritual history yet.",
            )
        }
        item {
            AddCustomRitualCard(
                palette = palette,
                onClick = onAddCustomRitualClick,
            )
        }
        item {
            RitualStreakCard(
                streakDays = overview.streakDays,
                streakMessage = overview.streakMessage,
                palette = palette,
            )
        }
    }
}

@Composable
private fun HeaderSection(
    palette: XendPalette,
    onCalendarClick: () -> Unit,
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
                    text = "Daily Ritual",
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
                text = "Little moments, every day.",
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
                .clickable(onClick = onCalendarClick),
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
                    imageVector = Heroicons.Outline.CalendarDays,
                    contentDescription = "Calendar",
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
private fun ProgressSummaryCard(
    overview: DailyRitualOverviewModel,
    palette: XendPalette,
) {
    val progress = if (overview.totalCount > 0) {
        (overview.completedCount.toFloat() / overview.totalCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        shape = RoundedCornerShape(22.dp),
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgressRing(
                completedCount = overview.completedCount,
                totalCount = overview.totalCount,
                progress = progress,
                palette = palette,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = overview.summaryTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )
                Text(
                    text = overview.summaryBody,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontSize = 13.sp,
                    ),
                    color = palette.mutedInk,
                )
            }
            SummaryArtwork(
                palette = palette,
                modifier = Modifier.size(84.dp),
            )
        }
    }
}

@Composable
private fun ProgressRing(
    completedCount: Int,
    totalCount: Int,
    progress: Float,
    palette: XendPalette,
) {
    Box(
        modifier = Modifier.size(76.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            val diameterOffset = stroke / 2f
            drawArc(
                color = palette.primarySoft,
                startAngle = -215f,
                sweepAngle = 250f,
                useCenter = false,
                topLeft = Offset(diameterOffset, diameterOffset),
                size = size.copy(
                    width = size.width - stroke,
                    height = size.height - stroke,
                ),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        palette.primaryBright,
                        palette.primary,
                    ),
                ),
                startAngle = -215f,
                sweepAngle = 250f * progress,
                useCenter = false,
                topLeft = Offset(diameterOffset, diameterOffset),
                size = size.copy(
                    width = size.width - stroke,
                    height = size.height - stroke,
                ),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = completedCount.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                    ),
                    color = palette.ink,
                )
                Text(
                    text = "/$totalCount",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodySmall,
                color = palette.mutedInk,
            )
        }
    }
}

@Composable
private fun SummaryArtwork(
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
                .padding(start = 6.dp, top = 12.dp),
            size = 8.dp,
        )
        FloatingHeart(
            tint = palette.primaryBright,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 6.dp, top = 12.dp),
            size = 7.dp,
        )
        FloatingHeart(
            tint = palette.primary,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 0.dp, top = 12.dp),
            size = 6.dp,
        )
        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 8.dp)
                .size(44.dp),
            shape = CircleShape,
            color = palette.primary,
            shadowElevation = 3.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.FaceSmile,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 6.dp)
                .size(42.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 3.dp,
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
        Surface(
            modifier = Modifier
                .size(34.dp),
            shape = CircleShape,
            color = palette.primarySoft,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.Heart,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
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

@Composable
private fun SectionHeader(
    title: String,
    palette: XendPalette,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Heroicons.Outline.Sparkles,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.ink,
            )
        }
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.primary,
                modifier = Modifier.clickable(onClick = onActionClick),
            )
        }
    }
}

@Composable
private fun RitualChecklistCard(
    rituals: List<DailyRitualHistoryItemModel>,
    palette: XendPalette,
    emptyText: String,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (rituals.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    ),
                    color = palette.mutedInk,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                )
            } else {
                rituals.forEachIndexed { index, item ->
                    RitualRow(
                        item = item,
                        palette = palette,
                    )
                    if (index != rituals.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = palette.outline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RitualRow(
    item: DailyRitualHistoryItemModel,
    palette: XendPalette,
) {
    val accent = ritualAccent(item.kind, palette)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = accent.background,
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = accent.icon,
                    contentDescription = null,
                    tint = accent.iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item.supportingLabel?.let { supportingLabel ->
                Text(
                    text = supportingLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                    ),
                    color = palette.primary,
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                ),
                color = palette.ink,
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 19.sp,
                    fontSize = 13.sp,
                ),
                color = palette.mutedInk,
            )
        }

        CompletionIndicator(
            completed = item.completed,
            palette = palette,
        )
    }
}

@Composable
private fun TodayRitualCard(
    ritual: DailyRitualTodayModel?,
    palette: XendPalette,
    isSubmitting: Boolean,
    onSubmitRitual: (String, String?) -> Unit,
    onOpenTextResponse: () -> Unit,
    onOpenImagePicker: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        if (ritual == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Today's Ritual",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )
                Text(
                    text = "No ritual is scheduled yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    ),
                    color = palette.mutedInk,
                )
            }
            return@Card
        }

        val accent = ritualAccent(ritual.kind, palette)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accent.background,
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = accent.icon,
                                contentDescription = null,
                                tint = accent.iconTint,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Today's Ritual",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                            ),
                            color = palette.primary,
                        )
                        Text(
                            text = ritual.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = palette.ink,
                        )
                    }
                }
                CompletionIndicator(
                    completed = ritual.completed,
                    palette = palette,
                )
            }

            Text(
                text = ritual.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                ),
                color = palette.mutedInk,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ritual.suggestedTime?.let { suggestedTime ->
                    RitualMetaChip(
                        label = suggestedTime,
                        palette = palette,
                    )
                }
                RitualMetaChip(
                    label = "+${ritual.rewardPoints} BP",
                    palette = palette,
                )
            }

            when {
                ritual.canSubmit && ritual.submissionType == "none" -> {
                    AppButton(
                        text = "Mark Done",
                        onClick = { onSubmitRitual(ritual.assignmentId, null) },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isSubmitting,
                        enabled = !isSubmitting,
                    )
                }

                ritual.canSubmit && ritual.submissionType == "text" -> {
                    AppButton(
                        text = "Write Response",
                        onClick = onOpenTextResponse,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting,
                    )
                }

                ritual.canSubmit && ritual.submissionType == "image" -> {
                    AppButton(
                        text = "Choose Photo",
                        onClick = onOpenImagePicker,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting,
                        isLoading = isSubmitting,
                    )
                }

                !ritual.statusLabel.isNullOrBlank() -> {
                    Text(
                        text = ritual.statusLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        ),
                        color = palette.mutedInk,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRitualResponseDialog(
    isSubmitting: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = {
            Text(
                text = "Write your response",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Send a short response to complete today's ritual.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    label = "Your response",
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSubmit,
                enabled = draft.isNotBlank() && !isSubmitting,
            ) {
                Text(if (isSubmitting) "Submitting..." else "Submit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
            ) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun RitualMetaChip(
    label: String,
    palette: XendPalette,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = palette.primarySoft,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            ),
            color = palette.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun CompletionIndicator(
    completed: Boolean,
    palette: XendPalette,
) {
    if (completed) {
        Surface(
            shape = CircleShape,
            color = palette.primary,
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Heroicons.Solid.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    } else {
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = palette.outline,
            ),
        ) {
            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}

private data class RitualAccentUi(
    val icon: ImageVector,
    val iconTint: Color,
    val background: Color,
)

@Composable
private fun ritualAccent(
    kind: RitualItemKind,
    palette: XendPalette,
): RitualAccentUi {
    return when (kind) {
        RitualItemKind.MorningMessage -> RitualAccentUi(
            icon = Heroicons.Outline.Heart,
            iconTint = palette.primary,
            background = palette.primarySoft,
        )
        RitualItemKind.CheckIn -> RitualAccentUi(
            icon = Heroicons.Outline.ChatBubbleLeftRight,
            iconTint = palette.lavender,
            background = palette.lavenderSoft,
        )
        RitualItemKind.GratitudeMoment -> RitualAccentUi(
            icon = Heroicons.Outline.Sun,
            iconTint = palette.orange,
            background = palette.orangeSoft,
        )
        RitualItemKind.SharePhoto -> RitualAccentUi(
            icon = Heroicons.Outline.Camera,
            iconTint = Color(0xFF53B992),
            background = Color(0xFFE6F7F0),
        )
        RitualItemKind.GoodNightMessage -> RitualAccentUi(
            icon = Heroicons.Outline.Sparkles,
            iconTint = Color(0xFFD177E8),
            background = Color(0xFFF7E9FF),
        )
    }
}

@Composable
private fun AddCustomRitualCard(
    palette: XendPalette,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            palette.primarySoft.copy(alpha = 0.9f),
                            palette.surface,
                        ),
                    ),
                )
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = palette.primarySoft,
                ),
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Add Custom Ritual",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                    ),
                    color = palette.primary,
                )
                Text(
                    text = "Create a ritual that's uniquely yours",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = palette.primary,
                )
            }
            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun RitualStreakCard(
    streakDays: Int,
    streakMessage: String,
    palette: XendPalette,
) {
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Ritual Streak",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = palette.ink,
                    )
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = streakDays.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp,
                        ),
                        color = palette.ink,
                    )
                    Text(
                        text = "days",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = palette.ink,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Text(
                    text = streakMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    ),
                    color = palette.mutedInk,
                )
            }
            StreakArtwork(
                palette = palette,
                modifier = Modifier.size(88.dp),
            )
        }
    }
}

@Composable
private fun StreakArtwork(
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
                .align(Alignment.CenterStart)
                .padding(start = 4.dp, top = 12.dp),
            size = 7.dp,
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
                .align(Alignment.CenterEnd)
                .padding(end = 0.dp, top = 6.dp),
            size = 6.dp,
        )

        Surface(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(16.dp),
            color = palette.primarySoft,
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(width = 6.dp, height = 10.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = palette.primary,
                        ) {}
                        Surface(
                            modifier = Modifier.size(width = 6.dp, height = 10.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = palette.primary,
                        ) {}
                    }
                    Icon(
                        imageVector = Heroicons.Outline.Heart,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
