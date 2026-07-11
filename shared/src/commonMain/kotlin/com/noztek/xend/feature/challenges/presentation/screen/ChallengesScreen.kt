package com.noztek.xend.feature.challenges.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ChatBubbleLeftRight
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Fire
import com.composables.icons.heroicons.outline.Gift
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.Moon
import com.composables.icons.heroicons.outline.Photo
import com.composables.icons.heroicons.outline.Sparkles
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.core.ui.media.rememberImagePickerLauncher
import com.noztek.xend.core.ui.theme.XendPalette
import com.noztek.xend.core.ui.theme.XendTheme
import com.noztek.xend.feature.challenges.domain.model.ChallengeAssignmentModel
import com.noztek.xend.feature.challenges.domain.model.ChallengeAudience
import com.noztek.xend.feature.challenges.domain.model.ChallengeCategory
import com.noztek.xend.feature.challenges.domain.model.ChallengeStatus
import com.noztek.xend.feature.challenges.domain.model.ChallengeSubmissionType
import com.noztek.xend.feature.challenges.domain.model.ChallengeTemplateModel
import com.noztek.xend.feature.challenges.domain.model.ChallengesOverviewModel
import com.noztek.xend.feature.challenges.presentation.state.ChallengesUiState
import com.noztek.xend.feature.challenges.presentation.viewmodel.ChallengesViewModel
import org.koin.compose.koinInject

@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val palette = XendTheme.palette
    val snackbarHostState = remember { SnackbarHostState() }
    val imagePickerLauncher = rememberImagePickerLauncher(
        onPicked = viewModel::submitImageCompletion,
        onUnavailable = viewModel::showMessage,
    )

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
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
                ChallengesContent(
                    state = state,
                    overview = requireNotNull(state.overview),
                    palette = palette,
                    onRefresh = viewModel::refresh,
                    onAudienceSelected = viewModel::onAudienceSelected,
                    onCategorySelected = viewModel::onCategorySelected,
                    onSendTemplate = viewModel::openSendComposer,
                    onAccept = viewModel::accept,
                    onDecline = viewModel::decline,
                    onCompleteWithoutSubmission = viewModel::completeWithoutSubmission,
                    onOpenTextCompletion = viewModel::openTextCompletion,
                    onOpenImageCompletion = { challenge ->
                        viewModel.prepareImageCompletion(challenge)
                        imagePickerLauncher()
                    },
                    submissionImages = state.submissionImages,
                    loadingSubmissionImageIds = state.loadingSubmissionImageIds,
                    onLoadSubmissionImage = viewModel::loadSubmissionImage,
                )
            }

            else -> {
                EmptyStateCard(
                    title = "Challenges are not ready yet",
                    body = state.message ?: "We couldn't load your couple challenges right now.",
                    palette = palette,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 18.dp),
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

    state.selectedTemplate?.let { template ->
        SendChallengeDialog(
            template = template,
            partnerName = state.overview?.partnerName.orEmpty(),
            noteDraft = state.noteDraft,
            isSubmitting = state.isSubmitting,
            onNoteChange = viewModel::onNoteDraftChanged,
            onDismiss = viewModel::dismissSendComposer,
            onSend = viewModel::sendSelectedChallenge,
        )
    }

    val completionChallenge = state.selectedCompletionChallenge
    if (completionChallenge != null && completionChallenge.submissionType == ChallengeSubmissionType.Text) {
        TextChallengeResponseDialog(
            challenge = completionChallenge,
            responseDraft = state.responseDraft,
            isSubmitting = state.isSubmitting,
            onDraftChange = viewModel::onResponseDraftChanged,
            onDismiss = viewModel::dismissCompletionComposer,
            onSubmit = viewModel::submitTextCompletion,
        )
    }
}

@Composable
private fun ChallengesContent(
    state: ChallengesUiState,
    overview: ChallengesOverviewModel,
    palette: XendPalette,
    onRefresh: () -> Unit,
    onAudienceSelected: (ChallengeAudience) -> Unit,
    onCategorySelected: (ChallengeCategory) -> Unit,
    onSendTemplate: (ChallengeTemplateModel) -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onCompleteWithoutSubmission: (String) -> Unit,
    onOpenTextCompletion: (ChallengeAssignmentModel) -> Unit,
    onOpenImageCompletion: (ChallengeAssignmentModel) -> Unit,
    submissionImages: Map<String, ImageBitmap>,
    loadingSubmissionImageIds: Set<String>,
    onLoadSubmissionImage: (String) -> Unit,
) {
    val incomingPending = rememberFilteredChallenges(overview.incoming, ChallengeStatus.Sent)
    val incomingActive = rememberFilteredChallenges(overview.incoming, ChallengeStatus.Accepted)
    val sentActive = overview.sent.filter { it.status == ChallengeStatus.Sent || it.status == ChallengeStatus.Accepted }
    val templates = overview.templates.filter {
        state.selectedCategory == ChallengeCategory.All || it.category == state.selectedCategory
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ChallengesHeader(
                palette = palette,
                onRefresh = onRefresh,
            )
        }
        item {
            AudienceTabs(
                selectedAudience = state.selectedAudience,
                palette = palette,
                onAudienceSelected = onAudienceSelected,
            )
        }
        item {
            ChallengeSummaryCard(
                selectedAudience = state.selectedAudience,
                partnerName = overview.partnerName,
                incomingPendingCount = incomingPending.size,
                incomingActiveCount = incomingActive.size,
                sentActiveCount = sentActive.size,
                palette = palette,
            )
        }

        if (state.selectedAudience == ChallengeAudience.ForThem) {
            item {
                SectionTitle(
                    title = "Challenge ideas for ${overview.partnerName}",
                    palette = palette,
                )
            }
            item {
                CategoryChips(
                    selectedCategory = state.selectedCategory,
                    palette = palette,
                    onCategorySelected = onCategorySelected,
                )
            }

            if (templates.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No challenges at this level yet",
                        body = "When your bond grows or more templates are added, they'll appear here.",
                        palette = palette,
                    )
                }
            } else {
                items(templates, key = { it.templateId }) { template ->
                    ChallengeTemplateCard(
                        template = template,
                        partnerName = overview.partnerName,
                        palette = palette,
                        isSubmitting = state.isSubmitting,
                        onSend = { onSendTemplate(template) },
                    )
                }
            }

            if (sentActive.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Already with ${overview.partnerName}",
                        palette = palette,
                    )
                }
                items(sentActive, key = { it.challengeId }) { challenge ->
                    ChallengeStatusCard(
                        challenge = challenge,
                        palette = palette,
                        submissionImage = submissionImages[challenge.challengeId],
                        isSubmissionImageLoading = challenge.challengeId in loadingSubmissionImageIds,
                        onLoadSubmissionImage = onLoadSubmissionImage,
                    )
                }
            }
        } else {
            if (incomingActive.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Active for you",
                        palette = palette,
                    )
                }
                items(incomingActive, key = { it.challengeId }) { challenge ->
                    ChallengeIncomingCard(
                        challenge = challenge,
                        palette = palette,
                        isSubmitting = state.isSubmitting,
                        onAccept = onAccept,
                        onDecline = onDecline,
                        onCompleteWithoutSubmission = onCompleteWithoutSubmission,
                        onOpenTextCompletion = onOpenTextCompletion,
                        onOpenImageCompletion = onOpenImageCompletion,
                    )
                }
            }

            if (incomingPending.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Waiting for your answer",
                        palette = palette,
                    )
                }
                items(incomingPending, key = { it.challengeId }) { challenge ->
                    ChallengeIncomingCard(
                        challenge = challenge,
                        palette = palette,
                        isSubmitting = state.isSubmitting,
                        onAccept = onAccept,
                        onDecline = onDecline,
                        onCompleteWithoutSubmission = onCompleteWithoutSubmission,
                        onOpenTextCompletion = onOpenTextCompletion,
                        onOpenImageCompletion = onOpenImageCompletion,
                    )
                }
            }

        }

        if (overview.history.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Recent history",
                    palette = palette,
                )
            }
            items(overview.history.take(8), key = { it.challengeId }) { challenge ->
                ChallengeStatusCard(
                    challenge = challenge,
                    palette = palette,
                    submissionImage = submissionImages[challenge.challengeId],
                    isSubmissionImageLoading = challenge.challengeId in loadingSubmissionImageIds,
                    onLoadSubmissionImage = onLoadSubmissionImage,
                )
            }
        }
    }
}

@Composable
private fun ChallengesHeader(
    palette: XendPalette,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Challenges",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                ),
                color = palette.ink,
            )
            Text(
                text = "Choose something intimate. Let your partner answer it.",
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
                .clickable(onClick = onRefresh),
            shape = RoundedCornerShape(15.dp),
            color = Color.White.copy(alpha = 0.82f),
            border = BorderStroke(
                width = 1.dp,
                color = palette.primarySoft,
            ),
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.Gift,
                    contentDescription = "Refresh challenges",
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
            modifier = Modifier.fillMaxWidth(),
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
private fun ChallengeSummaryCard(
    selectedAudience: ChallengeAudience,
    partnerName: String,
    incomingPendingCount: Int,
    incomingActiveCount: Int,
    sentActiveCount: Int,
    palette: XendPalette,
) {
    val title: String
    val body: String
    val icon: ImageVector

    if (selectedAudience == ChallengeAudience.ForYou) {
        title = when {
            incomingActiveCount > 0 -> "Something is already unfolding"
            incomingPendingCount > 0 -> "$partnerName reached for you"
            else -> "Nothing is waiting yet"
        }
        body = when {
            incomingActiveCount > 0 -> "You accepted $incomingActiveCount challenge${if (incomingActiveCount == 1) "" else "s"}. Finish one when the moment feels right."
            incomingPendingCount > 0 -> "$incomingPendingCount challenge${if (incomingPendingCount == 1) "" else "s"} need your answer. Accept what feels worth opening."
            else -> "When $partnerName sends something playful, bold, or private, it will arrive here."
        }
        icon = Heroicons.Outline.Heart
    } else {
        title = "Choose something for $partnerName"
        body = when {
            sentActiveCount > 0 -> "You already have $sentActiveCount challenge${if (sentActiveCount == 1) "" else "s"} in motion. Send another only if it adds something real."
            else -> "Challenges are meant to be chosen, not automated. Pick one that feels personal."
        }
        icon = Heroicons.Outline.Sparkles
    }

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
                            palette.primarySoft.copy(alpha = 0.58f),
                        ),
                    ),
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AccentCircle(
                icon = icon,
                tint = palette.primary,
                background = Color.White.copy(alpha = 0.74f),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    ),
                    color = palette.ink,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    palette: XendPalette,
) {
    Text(
        text = title,
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
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = listOf(
                ChallengeCategory.All,
                ChallengeCategory.Soft,
                ChallengeCategory.Desire,
                ChallengeCategory.Private,
                ChallengeCategory.Bold,
                ChallengeCategory.Devotion,
            ),
            key = { it.name },
        ) { category ->
            val selected = category == selectedCategory
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) palette.primarySoft else palette.surface,
                border = if (selected) {
                    BorderStroke(1.dp, palette.primarySoft)
                } else {
                    BorderStroke(1.dp, palette.outline)
                },
            ) {
                Text(
                    text = category.label(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selected) palette.primary else palette.mutedInk,
                )
            }
        }
    }
}

@Composable
private fun ChallengeTemplateCard(
    template: ChallengeTemplateModel,
    partnerName: String,
    palette: XendPalette,
    isSubmitting: Boolean,
    onSend: () -> Unit,
) {
    val accent = categoryAccent(template.category, palette)

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
                        text = template.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink,
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        ),
                        color = palette.mutedInk,
                    )
                }
                StatusPill(
                    text = "+${template.rewardPoints} BP",
                    palette = palette,
                    positive = false,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MiniInfoChip(
                    text = template.submissionType.label(),
                    palette = palette,
                )
                template.expiryLabel?.let {
                    MiniInfoChip(
                        text = it,
                        palette = palette,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    modifier = Modifier.clickable(enabled = !isSubmitting, onClick = onSend),
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White,
                    border = BorderStroke(
                        width = 1.dp,
                        color = palette.primarySoft,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Send",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSubmitting) palette.mutedInk else palette.primary,
                        )
                        Icon(
                            imageVector = Heroicons.Outline.ChevronRight,
                            contentDescription = null,
                            tint = if (isSubmitting) palette.mutedInk else palette.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeIncomingCard(
    challenge: ChallengeAssignmentModel,
    palette: XendPalette,
    isSubmitting: Boolean,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onCompleteWithoutSubmission: (String) -> Unit,
    onOpenTextCompletion: (ChallengeAssignmentModel) -> Unit,
    onOpenImageCompletion: (ChallengeAssignmentModel) -> Unit,
) {
    val accent = categoryAccent(challenge.category, palette)

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
                        text = challenge.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink,
                    )
                    Text(
                        text = "From ${challenge.senderDisplayName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.primary,
                    )
                }
                StatusPill(
                    text = challenge.status.label(),
                    palette = palette,
                    positive = challenge.status == ChallengeStatus.Accepted,
                )
            }

            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
                color = palette.mutedInk,
            )

            challenge.note?.let { note ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = palette.surfaceSoft,
                ) {
                    Text(
                        text = "\"$note\"",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        ),
                        color = palette.ink,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MiniInfoChip(
                    text = challenge.submissionType.label(),
                    palette = palette,
                )
                MiniInfoChip(
                    text = "+${challenge.rewardPoints} BP",
                    palette = palette,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = challenge.expiresAtLabel ?: challenge.createdAtLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.mutedInk,
                )
            }

            when {
                challenge.canAccept -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppButton(
                            text = "Accept",
                            onClick = { onAccept(challenge.challengeId) },
                            enabled = !isSubmitting,
                            isLoading = false,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { onDecline(challenge.challengeId) },
                            enabled = !isSubmitting,
                        ) {
                            Text(
                                text = "Decline",
                                color = palette.mutedInk,
                            )
                        }
                    }
                }

                challenge.canComplete -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppButton(
                            text = challenge.submissionType.primaryActionLabel(),
                            onClick = {
                                when (challenge.submissionType) {
                                    ChallengeSubmissionType.None -> onCompleteWithoutSubmission(challenge.challengeId)
                                    ChallengeSubmissionType.Text -> onOpenTextCompletion(challenge)
                                    ChallengeSubmissionType.Image -> onOpenImageCompletion(challenge)
                                }
                            },
                            enabled = !isSubmitting,
                            isLoading = false,
                            modifier = Modifier.weight(1f),
                        )
                        if (challenge.canDecline) {
                            TextButton(
                                onClick = { onDecline(challenge.challengeId) },
                                enabled = !isSubmitting,
                            ) {
                                Text(
                                    text = "Decline",
                                    color = palette.mutedInk,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeStatusCard(
    challenge: ChallengeAssignmentModel,
    palette: XendPalette,
    submissionImage: ImageBitmap?,
    isSubmissionImageLoading: Boolean,
    onLoadSubmissionImage: (String) -> Unit,
) {
    val accent = categoryAccent(challenge.category, palette)
    var isPhotoDialogOpen by rememberSaveable(challenge.challengeId) { mutableStateOf(false) }
    val counterpartLabel = when (challenge.status) {
        ChallengeStatus.Completed,
        ChallengeStatus.Declined,
        ChallengeStatus.Expired,
        ChallengeStatus.Cancelled,
        -> "With ${challenge.receiverDisplayName.ifBlank { challenge.senderDisplayName }}"
        ChallengeStatus.Accepted,
        ChallengeStatus.Sent,
        -> "For ${challenge.receiverDisplayName}"
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
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
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.ink,
                    )
                    Text(
                        text = counterpartLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.mutedInk,
                    )
                }
                StatusPill(
                    text = challenge.status.label(),
                    palette = palette,
                    positive = challenge.status == ChallengeStatus.Completed,
                )
            }

            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
                color = palette.mutedInk,
            )

            challenge.submissionTextResponse?.let { response ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = palette.surfaceSoft,
                    border = BorderStroke(1.dp, palette.outline),
                ) {
                    Text(
                        text = response,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        ),
                        color = palette.ink,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    challenge.hasSubmissionImage -> {
                        ActionMiniChip(
                            text = "View photo",
                            palette = palette,
                            onClick = {
                                isPhotoDialogOpen = true
                                if (submissionImage == null) {
                                    onLoadSubmissionImage(challenge.challengeId)
                                }
                            },
                        )
                    }

                    challenge.submissionType != ChallengeSubmissionType.None -> {
                        MiniInfoChip(
                            text = challenge.submissionType.label(),
                            palette = palette,
                        )
                    }
                }
                MiniInfoChip(
                    text = "+${challenge.rewardPoints} BP",
                    palette = palette,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = challenge.expiresAtLabel ?: challenge.createdAtLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.mutedInk,
                )
            }
        }
    }

    if (isPhotoDialogOpen) {
        SubmissionImageDialog(
            palette = palette,
            submissionImage = submissionImage,
            isSubmissionImageLoading = isSubmissionImageLoading,
            onDismiss = { isPhotoDialogOpen = false },
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    palette: XendPalette,
    positive: Boolean,
) {
    val background = if (positive) Color(0xFFE8F7EE) else palette.primarySoft
    val content = if (positive) Color(0xFF208A50) else palette.primary

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = content,
        )
    }
}

@Composable
private fun MiniInfoChip(
    text: String,
    palette: XendPalette,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = palette.surfaceSoft,
        border = BorderStroke(1.dp, palette.outline),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = palette.ink,
        )
    }
}

@Composable
private fun ActionMiniChip(
    text: String,
    palette: XendPalette,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = palette.surfaceSoft,
        border = BorderStroke(1.dp, palette.outline),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = palette.ink,
        )
    }
}

@Composable
private fun SubmissionImageDialog(
    palette: XendPalette,
    submissionImage: ImageBitmap?,
    isSubmissionImageLoading: Boolean,
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
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = palette.surface,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Photo response",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.ink,
                )

                when {
                    submissionImage != null -> {
                        Image(
                            bitmap = submissionImage,
                            contentDescription = "Challenge submission image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = palette.outline,
                                    shape = RoundedCornerShape(20.dp),
                                ),
                            contentScale = ContentScale.Fit,
                        )
                    }

                    isSubmissionImageLoading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = palette.primary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading photo...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.mutedInk,
                            )
                        }
                    }

                    else -> {
                        Text(
                            text = "Photo unavailable right now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.mutedInk,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Close",
                            color = palette.mutedInk,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    body: String,
    palette: XendPalette,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.ink,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
                color = palette.mutedInk,
            )
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

private data class AccentSpec(
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
)

@Composable
private fun categoryAccent(
    category: ChallengeCategory,
    palette: XendPalette,
): AccentSpec {
    return when (category) {
        ChallengeCategory.Desire -> AccentSpec(
            icon = Heroicons.Outline.Fire,
            tint = palette.primary,
            background = palette.primarySoft,
        )
        ChallengeCategory.Private -> AccentSpec(
            icon = Heroicons.Outline.Photo,
            tint = palette.lavender,
            background = palette.lavenderSoft,
        )
        ChallengeCategory.Bold -> AccentSpec(
            icon = Heroicons.Outline.Moon,
            tint = palette.orange,
            background = palette.orangeSoft,
        )
        ChallengeCategory.Devotion -> AccentSpec(
            icon = Heroicons.Outline.Heart,
            tint = palette.primary,
            background = palette.primarySoft,
        )
        ChallengeCategory.All,
        ChallengeCategory.Soft,
        -> AccentSpec(
            icon = Heroicons.Outline.ChatBubbleLeftRight,
            tint = palette.lavender,
            background = palette.lavenderSoft,
        )
    }
}

@Composable
private fun SendChallengeDialog(
    template: ChallengeTemplateModel,
    partnerName: String,
    noteDraft: String,
    isSubmitting: Boolean,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Send to $partnerName",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AppTextField(
                    value = noteDraft,
                    onValueChange = onNoteChange,
                    label = "Optional note",
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Send",
                onClick = onSend,
                enabled = !isSubmitting,
                isLoading = isSubmitting,
                modifier = Modifier.width(112.dp),
            )
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
private fun TextChallengeResponseDialog(
    challenge: ChallengeAssignmentModel,
    responseDraft: String,
    isSubmitting: Boolean,
    onDraftChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppTextField(
                    value = responseDraft,
                    onValueChange = onDraftChange,
                    label = "Write your response",
                    singleLine = false,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Submit",
                onClick = onSubmit,
                enabled = !isSubmitting,
                isLoading = isSubmitting,
                modifier = Modifier.width(118.dp),
            )
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

private fun ChallengeCategory.label(): String {
    return when (this) {
        ChallengeCategory.All -> "All"
        ChallengeCategory.Soft -> "Soft"
        ChallengeCategory.Desire -> "Desire"
        ChallengeCategory.Private -> "Private"
        ChallengeCategory.Bold -> "Bold"
        ChallengeCategory.Devotion -> "Devotion"
    }
}

private fun ChallengeSubmissionType.label(): String {
    return when (this) {
        ChallengeSubmissionType.None -> "No proof needed"
        ChallengeSubmissionType.Text -> "Text response"
        ChallengeSubmissionType.Image -> "Photo response"
    }
}

private fun ChallengeSubmissionType.primaryActionLabel(): String {
    return when (this) {
        ChallengeSubmissionType.None -> "Mark done"
        ChallengeSubmissionType.Text -> "Write response"
        ChallengeSubmissionType.Image -> "Send photo"
    }
}

private fun ChallengeStatus.label(): String {
    return when (this) {
        ChallengeStatus.Sent -> "Pending"
        ChallengeStatus.Accepted -> "Accepted"
        ChallengeStatus.Completed -> "Completed"
        ChallengeStatus.Declined -> "Declined"
        ChallengeStatus.Expired -> "Expired"
        ChallengeStatus.Cancelled -> "Cancelled"
    }
}

private fun rememberFilteredChallenges(
    items: List<ChallengeAssignmentModel>,
    status: ChallengeStatus,
): List<ChallengeAssignmentModel> = items.filter { it.status == status }
