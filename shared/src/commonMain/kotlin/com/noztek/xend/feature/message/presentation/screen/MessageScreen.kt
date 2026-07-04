package com.noztek.xend.feature.message.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ArrowUturnLeft
import com.composables.icons.heroicons.outline.ArrowUturnRight
import com.composables.icons.heroicons.outline.Camera
import com.composables.icons.heroicons.outline.DocumentDuplicate
import com.composables.icons.heroicons.outline.EllipsisVertical
import com.composables.icons.heroicons.outline.FaceSmile
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Microphone
import com.composables.icons.heroicons.outline.PaperAirplane
import com.composables.icons.heroicons.outline.Phone
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.Trash
import com.composables.icons.heroicons.outline.VideoCamera
import com.composables.icons.heroicons.outline.XMark
import com.composables.icons.heroicons.solid.Check
import com.composables.icons.heroicons.solid.CheckCircle
import com.composables.icons.heroicons.solid.User
import com.noztek.xend.core.time.currentEpochSeconds
import com.noztek.xend.feature.message.domain.model.ChatMessageModel
import com.noztek.xend.feature.message.presentation.state.MessageUiState
import com.noztek.xend.feature.message.presentation.viewmodel.MessageViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import org.koin.compose.koinInject
import androidx.compose.runtime.collectAsState
import kotlinx.datetime.number

private enum class MessageQuickAction {
    Reply,
    Forward,
    Copy,
    Pin,
    Delete,
}

private data class MessageReactionUi(
    val emoji: String,
    val count: Int = 1,
)

private data class MessageActionItem(
    val label: String,
    val action: MessageQuickAction,
    val icon: ImageVector,
)

private sealed interface ChatTimelineItem {
    data class DateSeparator(val label: String) : ChatTimelineItem
    data class MessageItem(
        val message: ChatMessageModel,
        val showMetadata: Boolean,
        val bubbleGroupPosition: BubbleGroupPosition,
    ) : ChatTimelineItem
}

private enum class BubbleGroupPosition {
    Single,
    First,
    Middle,
    Last,
}

private val MessageActionOverlayMinWidth = 220.dp
private val MessageActionOverlayMaxWidth = 260.dp

@Composable
fun MessageScreen(
    conversationId: String,
    onBackClick: () -> Unit,
) {
    val vm = koinInject<MessageViewModel>()
    val state by vm.state.collectAsState()

    MessageScreenContent(
        state = state,
        conversationId = conversationId,
        onBackClick = onBackClick,
        onLoad = vm::load,
        onRefresh = vm::refresh,
        onRetry = vm::retry,
        onToggleReaction = { messageId, emoji, remove ->
            vm.toggleReaction(messageId, emoji, remove)
        },
        onSendTyping = vm::sendTyping,
        onSend = vm::send,
        deliveredStatusIcon = { tint ->
            DeliveredStatusIcon(tint = tint)
        },
    )
}

@Composable
fun MessageScreenContent(
    state: MessageUiState,
    conversationId: String,
    onBackClick: () -> Unit,
    onLoad: (String) -> Unit,
    onRefresh: (String) -> Unit,
    onRetry: (String, String) -> Unit,
    onToggleReaction: (messageId: String, emoji: String, remove: Boolean) -> Unit,
    onSendTyping: (String, Boolean) -> Unit,
    onSend: (conversationId: String, text: String, replyToMessageId: String?, onSent: () -> Unit) -> Unit,
    deliveredStatusIcon: @Composable (Color) -> Unit,
) {
    val listState = rememberLazyListState()
    val timelineItems = remember(state.items) { buildTimelineItems(state.items) }
    val typingBottomPadding by animateDpAsState(
        targetValue = if (state.isTyping) 22.dp else 12.dp,
        animationSpec = tween(durationMillis = 220),
        label = "typingBottomPadding",
    )
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    var draft by remember { mutableStateOf("") }
    var typingSent by remember { mutableStateOf(false) }
    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    var replyTargetMessage by remember { mutableStateOf<ChatMessageModel?>(null) }
    val isActionMode = selectedMessageId != null

    LaunchedEffect(conversationId) {
        onLoad(conversationId)
    }

    LaunchedEffect(timelineItems.size, state.sending) {
        if (timelineItems.isNotEmpty()) {
            listState.animateScrollToItem(timelineItems.lastIndex)
        }
    }

    LaunchedEffect(conversationId, draft) {
        val hasText = draft.isNotBlank()
        if (!hasText) {
            if (typingSent) {
                onSendTyping(conversationId, false)
                typingSent = false
            }
            return@LaunchedEffect
        }

        if (!typingSent) {
            onSendTyping(conversationId, true)
            typingSent = true
        }

        delay(1600)
        if (typingSent && isActive) {
            onSendTyping(conversationId, false)
            typingSent = false
        }
    }

    DisposableEffect(conversationId) {
        onDispose {
            typingSent = false
            onSendTyping(conversationId, false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ChatTopBar(
            title = state.header?.title ?: "Messages",
            subtitle = if (state.isTyping) "typing..." else state.presenceLabel ?: state.header?.subtitle,
            onBackClick = onBackClick,
            onTapWhileActionMode = { if (isActionMode) selectedMessageId = null },
        )

        if (!state.message.isNullOrBlank()) {
            Text(
                text = state.message.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (isActionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.42f))
                        .clickable { selectedMessageId = null },
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { selectedMessageId = null },
                )
            }
            if (timelineItems.isEmpty() && !state.isLoading) {
                Text(
                    text = "No messages yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    reverseLayout = false,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 12.dp,
                        end = 16.dp,
                        bottom = typingBottomPadding,
                    ),
                ) {
                    items(
                        items = timelineItems,
                        key = { item ->
                            when (item) {
                                is ChatTimelineItem.DateSeparator -> "date-${item.label}"
                                is ChatTimelineItem.MessageItem -> item.message.messageId
                            }
                        },
                    ) { item ->
                        when (item) {
                            is ChatTimelineItem.DateSeparator -> DateChip(label = item.label)
                            is ChatTimelineItem.MessageItem -> MessageBubble(
                                item = item.message,
                                showMetadata = item.showMetadata,
                                bubbleGroupPosition = item.bubbleGroupPosition,
                                isMine = item.message.senderUserId == state.currentUserId,
                                isRetrying = state.retryingMessageId == item.message.messageId,
                                isActionMode = isActionMode,
                                isSelected = selectedMessageId == item.message.messageId,
                                onRetryClick = {
                                    onRetry(conversationId, item.message.messageId)
                                },
                                onLongPress = {
                                    selectedMessageId = item.message.messageId
                                },
                                onDismissActionMode = {
                                    selectedMessageId = null
                                },
                                onActionSelected = { action, message ->
                                    when (action) {
                                        MessageQuickAction.Reply -> replyTargetMessage = message
                                        else -> Unit
                                    }
                                    selectedMessageId = null
                                },
                                reaction = item.message.reactions
                                    .groupBy { it.emoji }
                                    .maxByOrNull { it.value.size }
                                    ?.let { MessageReactionUi(emoji = it.key, count = it.value.size) },
                                onReactionSelected = { emoji, message ->
                                    val current = message.reactions.firstOrNull { it.userId == state.currentUserId }
                                    val remove = current?.emoji == emoji
                                    onToggleReaction(
                                        message.messageId,
                                        emoji,
                                        remove,
                                    )
                                    onRefresh(conversationId)
                                    selectedMessageId = null
                                },
                                deliveredStatusIcon = deliveredStatusIcon,
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = if (isImeVisible) 4.dp else 0.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (state.isTyping) {
                    TypingIndicatorChip(
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
                    )
                } else {
                    Spacer(modifier = Modifier.size(0.dp))
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 12.dp,
                                end = 12.dp,
                                top = 10.dp,
                                bottom = 10.dp,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        ChatComposer(
                            draft = draft,
                            onDraftChange = { draft = it },
                            isSending = state.sending,
                            isActionMode = isActionMode,
                            onTapWhileActionMode = { selectedMessageId = null },
                            replyTargetMessage = replyTargetMessage,
                            onClearReplyTarget = { replyTargetMessage = null },
                            onSendClick = {
                                val text = draft.trim()
                                if (text.isNotBlank()) {
                                    onSendTyping(conversationId, false)
                                    typingSent = false
                                    onSend(conversationId, text, replyTargetMessage?.messageId) {
                                        draft = ""
                                        replyTargetMessage = null
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    item: ChatMessageModel,
    showMetadata: Boolean,
    bubbleGroupPosition: BubbleGroupPosition,
    isMine: Boolean,
    isRetrying: Boolean,
    isActionMode: Boolean,
    isSelected: Boolean,
    onRetryClick: () -> Unit,
    onLongPress: () -> Unit,
    onDismissActionMode: () -> Unit,
    onActionSelected: (MessageQuickAction, ChatMessageModel) -> Unit,
    reaction: MessageReactionUi?,
    onReactionSelected: (String, ChatMessageModel) -> Unit,
    deliveredStatusIcon: @Composable (Color) -> Unit,
) {
    val bubbleVerticalPadding = when (bubbleGroupPosition) {
        BubbleGroupPosition.Single -> 6.dp
        BubbleGroupPosition.First,
        BubbleGroupPosition.Middle,
        BubbleGroupPosition.Last -> 0.dp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = bubbleVerticalPadding),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(140)) +
                slideInVertically(animationSpec = tween(180), initialOffsetY = { it / 3 }) +
                scaleIn(animationSpec = tween(180), initialScale = 0.96f),
            exit = fadeOut(animationSpec = tween(100)) +
                slideOutVertically(animationSpec = tween(130), targetOffsetY = { it / 4 }) +
                scaleOut(animationSpec = tween(120), targetScale = 0.98f),
        ) {
            Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
                ReactionBar(
                    onDismiss = onDismissActionMode,
                    onReactionSelected = { emoji -> onReactionSelected(emoji, item) },
                )
                Spacer(modifier = Modifier.size(6.dp))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = isActionMode,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onDismissActionMode() },
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        ) {
            val bubbleColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isMine -> MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                else -> MaterialTheme.colorScheme.surfaceContainer
            }
            val textColor = if (isSelected || isMine) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val metaColor = if (isSelected || isMine) {
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box {
                Card(
                    colors = CardDefaults.cardColors(containerColor = bubbleColor),
                    shape = bubbleShape(isMine, bubbleGroupPosition),
                    modifier = Modifier
                        .widthIn(
                            min = if (showMetadata) 120.dp else if (item.body.length <= 4) 0.dp else if (item.body.length <= 10) 56.dp else 0.dp,
                            max = when {
                                item.body.length <= 10 -> 180.dp
                                item.body.length <= 18 -> 220.dp
                                item.body.length <= 40 -> 280.dp
                                else -> 312.dp
                            },
                        )
                        .combinedClickable(
                            onClick = { if (isActionMode) onDismissActionMode() },
                            onLongClick = onLongPress,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 14.dp,
                            end = 14.dp,
                            top = 8.dp,
                            bottom = 8.dp,
                        ),
                    ) {
                        if (item.replyToMessageId != null) {
                            Surface(
                                color = if (isMine || isSelected) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                            ) {
                                Text(
                                    text = item.replyPreviewText?.takeIf { it.isNotBlank() } ?: "Replied message",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isMine || isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                )
                            }
                        }
                        Text(
                            text = item.body,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(
                                end = if (showMetadata) {
                                    when {
                                        item.body.length <= 6 -> 84.dp
                                        item.body.length <= 12 -> 76.dp
                                        else -> 72.dp
                                    }
                                } else {
                                    0.dp
                                },
                            ),
                        )

                        if (showMetadata) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = formatMessageTime(item.sentAtEpochSeconds ?: item.createdAtEpochSeconds),
                                    color = metaColor,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                    ),
                                )

                                if (isMine) {
                                    ReceiptStatus(
                                        message = item,
                                        isRetrying = isRetrying,
                                        tint = metaColor,
                                        onRetryClick = onRetryClick,
                                        deliveredStatusIcon = deliveredStatusIcon,
                                    )
                                }
                            }
                        }
                    }
                }
                if (reaction != null) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = 16.dp),
                    ) {
                        Text(
                            text = if (reaction.count > 1) "${reaction.emoji} ${reaction.count}" else reaction.emoji,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
        if (isActionMode && !isSelected) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .heightIn(min = 0.dp),
            )
        }
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(150)) +
                slideInVertically(animationSpec = tween(190), initialOffsetY = { -it / 4 }) +
                scaleIn(animationSpec = tween(190), initialScale = 0.97f),
            exit = fadeOut(animationSpec = tween(100)) +
                slideOutVertically(animationSpec = tween(130), targetOffsetY = { -it / 5 }) +
                scaleOut(animationSpec = tween(120), targetScale = 0.985f),
        ) {
            Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
                Spacer(modifier = Modifier.size(8.dp))
                MessageHoverMenu(
                    onDismiss = onDismissActionMode,
                    onActionSelected = { action -> onActionSelected(action, item) },
                )
            }
        }
    }
}

@Composable
private fun ReactionBar(
    onDismiss: () -> Unit,
    onReactionSelected: (String) -> Unit,
) {
    val emojis = listOf("❤️", "👍", "👎", "😂", "😮", "😢")
    Surface(
        modifier = Modifier.widthIn(
            min = MessageActionOverlayMinWidth,
            max = MessageActionOverlayMaxWidth,
        ),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            emojis.forEachIndexed { index, emoji ->
                var entered by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 36L)
                    entered = true
                }
                val rise by animateDpAsState(
                    targetValue = if (entered) 0.dp else 6.dp,
                    animationSpec = tween(
                        durationMillis = 260,
                        easing = FastOutSlowInEasing,
                    ),
                    label = "reactionRise$index",
                )
                val alpha by animateFloatAsState(
                    targetValue = if (entered) 1f else 0.65f,
                    animationSpec = tween(
                        durationMillis = 240,
                        easing = FastOutSlowInEasing,
                    ),
                    label = "reactionAlpha$index",
                )
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .offset(y = rise)
                        .clickable { onReactionSelected(emoji) },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.clickable(onClick = onDismiss),
            ) {
                Text(
                    text = "⋯",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun MessageHoverMenu(
    onDismiss: () -> Unit,
    onActionSelected: (MessageQuickAction) -> Unit,
) {
    val actions = buildList {
        add(MessageActionItem("Reply", MessageQuickAction.Reply, Heroicons.Outline.ArrowUturnLeft))
        add(MessageActionItem("Forward", MessageQuickAction.Forward, Heroicons.Outline.ArrowUturnRight))
        add(MessageActionItem("Copy", MessageQuickAction.Copy, Heroicons.Outline.DocumentDuplicate))
        add(MessageActionItem("Pin", MessageQuickAction.Pin, Heroicons.Outline.MapPin))
        add(MessageActionItem("Delete", MessageQuickAction.Delete, Heroicons.Outline.Trash))
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 10.dp,
        modifier = Modifier.widthIn(
            min = MessageActionOverlayMinWidth,
            max = MessageActionOverlayMaxWidth,
        ),
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            actions.forEach { action ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onActionSelected(action.action)
                            onDismiss()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyPreviewBar(
    message: ChatMessageModel,
    modifier: Modifier = Modifier,
    onClear: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = 25.dp,
            topEnd = 25.dp,
            bottomStart = 5.dp,
            bottomEnd = 5.dp,
        ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
            ) {
                Text(
                    text = "Replying to ${if (message.senderUserId.isNotBlank()) "message" else "chat"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = message.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Heroicons.Outline.XMark,
                    contentDescription = "Cancel reply",
                )
            }
        }
    }
}

@Composable
private fun DateChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ReceiptStatus(
    message: ChatMessageModel,
    isRetrying: Boolean,
    tint: Color,
    onRetryClick: () -> Unit,
    deliveredStatusIcon: @Composable (Color) -> Unit,
) {
    when {
        isRetrying || message.syncState == "pending" || message.syncState == "failed" -> {
            Text(
                text = messageStatusLabel(message, isRetrying),
                color = if (message.syncState == "failed") MaterialTheme.colorScheme.error else tint,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                modifier = if (message.syncState == "failed" && !isRetrying) {
                    Modifier.clickable(onClick = onRetryClick)
                } else {
                    Modifier
                },
            )
        }

        message.receiptStatus == "read" -> {
            Icon(
                imageVector = Heroicons.Solid.CheckCircle,
                contentDescription = "Read",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(14.dp),
            )
        }

        message.receiptStatus == "delivered" || message.syncState == "synced" -> {
            Box(modifier = Modifier.size(width = 15.dp, height = 12.dp)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-1).dp),
                ) {
                    deliveredStatusIcon(tint.copy(alpha = 0.9f))
                }
            }
        }

        else -> {
            Icon(
                imageVector = Heroicons.Solid.Check,
                contentDescription = "Sent",
                tint = tint,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun ChatTopBar(
    title: String,
    subtitle: String?,
    onBackClick: () -> Unit,
    onTapWhileActionMode: () -> Unit = {},
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onTapWhileActionMode,
                )
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Heroicons.Outline.ArrowLeft,
                    contentDescription = "Back",
                )
            }

            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Solid.User,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    if (subtitle == "online") {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Heroicons.Outline.VideoCamera,
                    contentDescription = "Video call",
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Heroicons.Outline.Phone,
                    contentDescription = "Voice call",
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Heroicons.Outline.EllipsisVertical,
                    contentDescription = "More",
                )
            }
        }
    }
}

@Composable
private fun TypingIndicatorChip(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "typingDots")
    val periodMillis = 1050

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val progress by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = periodMillis, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "typingDotProgress$index",
                )
                val phased = (progress + (index * 0.18f)) % 1f
                val eased = ((sin((phased * 2f * PI) - (PI / 2)).toFloat()) + 1f) / 2f
                val alpha = 0.35f + (0.6f * eased)
                Box(
                    modifier = Modifier.size(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .scale(0.78f + (0.22f * eased))
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    isSending: Boolean,
    isActionMode: Boolean,
    onTapWhileActionMode: () -> Unit,
    replyTargetMessage: ChatMessageModel?,
    onClearReplyTarget: () -> Unit,
    onSendClick: () -> Unit,
) {
    val hasText = draft.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isActionMode,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onTapWhileActionMode,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (replyTargetMessage != null) {
                    ReplyPreviewBar(
                        message = replyTargetMessage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp),
                        onClear = onClearReplyTarget,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .padding(start = 12.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Heroicons.Outline.FaceSmile,
                        contentDescription = "Emoji",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    BasicTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions.Default,
                        visualTransformation = VisualTransformation.None,
                        decorationBox = { innerTextField ->
                            if (draft.isBlank()) {
                                Text(
                                    text = "Xend message",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            innerTextField()
                        },
                    )
                    if (!hasText) {
                        Row(
                            modifier = Modifier.padding(start = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Heroicons.Outline.Camera,
                                contentDescription = "Camera",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Icon(
                                imageVector = Heroicons.Outline.Microphone,
                                contentDescription = "Voice note",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (hasText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (hasText) {
                    IconButton(
                        onClick = onSendClick,
                        modifier = Modifier.fillMaxSize(),
                        enabled = !isSending,
                    ) {
                        Icon(
                            imageVector = Heroicons.Outline.PaperAirplane,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                } else {
                    Icon(
                        imageVector = Heroicons.Outline.Plus,
                        contentDescription = "More actions",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

private fun buildTimelineItems(messages: List<ChatMessageModel>): List<ChatTimelineItem> {
    val items = mutableListOf<ChatTimelineItem>()
    var lastDateLabel: String? = null

    messages.forEachIndexed { index, message ->
        val messageEpoch = message.sentAtEpochSeconds ?: message.createdAtEpochSeconds
        val dateLabel = formatMessageDate(messageEpoch)
        if (dateLabel != lastDateLabel) {
            items += ChatTimelineItem.DateSeparator(dateLabel)
            lastDateLabel = dateLabel
        }

        val previous = messages.getOrNull(index - 1)
        val next = messages.getOrNull(index + 1)
        val sameGroupAsPrevious = previous?.let { areGroupedTogether(it, message) } == true
        val showMetadata = when {
            next == null -> true
            !areGroupedTogether(message, next) -> true
            else -> false
        }
        val sameGroupAsNext = next?.let { areGroupedTogether(message, it) } == true
        val bubbleGroupPosition = when {
            !sameGroupAsPrevious && !sameGroupAsNext -> BubbleGroupPosition.Single
            !sameGroupAsPrevious && sameGroupAsNext -> BubbleGroupPosition.First
            sameGroupAsPrevious && sameGroupAsNext -> BubbleGroupPosition.Middle
            else -> BubbleGroupPosition.Last
        }

        items += ChatTimelineItem.MessageItem(message, showMetadata, bubbleGroupPosition)
    }

    return items
}

private fun areGroupedTogether(first: ChatMessageModel, second: ChatMessageModel): Boolean {
    if (first.senderUserId != second.senderUserId) return false

    val firstEpoch = first.sentAtEpochSeconds ?: first.createdAtEpochSeconds
    val secondEpoch = second.sentAtEpochSeconds ?: second.createdAtEpochSeconds

    if (formatMessageDate(firstEpoch) != formatMessageDate(secondEpoch)) return false

    return abs(secondEpoch - firstEpoch) <= 300
}

private fun bubbleShape(isMine: Boolean, position: BubbleGroupPosition): RoundedCornerShape {
    val full = 30.dp
    val large = 14.dp
    val compact = 3.dp

    return if (isMine) {
        when (position) {
            BubbleGroupPosition.Single -> RoundedCornerShape(full)
            BubbleGroupPosition.First -> RoundedCornerShape(full, large, full, compact)
            BubbleGroupPosition.Middle -> RoundedCornerShape(full, compact, full, compact)
            BubbleGroupPosition.Last -> RoundedCornerShape(full, compact, full, large)
        }
    } else {
        when (position) {
            BubbleGroupPosition.Single -> RoundedCornerShape(full)
            BubbleGroupPosition.First -> RoundedCornerShape(large, full, compact, full)
            BubbleGroupPosition.Middle -> RoundedCornerShape(compact, full, compact, full)
            BubbleGroupPosition.Last -> RoundedCornerShape(compact, full, large, full)
        }
    }
}

private fun messageStatusLabel(message: ChatMessageModel, isRetrying: Boolean): String = when {
    isRetrying -> "Retrying..."
    message.syncState == "pending" -> "Sending..."
    message.syncState == "failed" -> "Tap to retry"
    message.receiptStatus == "read" -> "Read"
    message.receiptStatus == "delivered" -> "Delivered"
    message.syncState == "synced" -> "Delivered"
    message.syncState == "sent" -> "Sent"
    else -> message.syncState
}

private fun formatMessageTime(epochSeconds: Long): String {
    val local = Instant.fromEpochSeconds(epochSeconds)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
    val hour = local.hour % 12
    val displayHour = if (hour == 0) 12 else hour
    val minute = local.minute.toString().padStart(2, '0')
    val period = if (local.hour >= 12) "PM" else "AM"
    return "$displayHour:$minute $period"
}

private fun formatMessageDate(epochSeconds: Long): String {
    val zone = TimeZone.currentSystemDefault()
    val date = Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(zone).date
    val today = Instant.fromEpochSeconds(currentEpochSeconds()).toLocalDateTime(zone).date
    val yesterday = Instant.fromEpochSeconds(currentEpochSeconds() - 86_400).toLocalDateTime(zone).date

    return when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> formatCalendarDate(date)
    }
}

private fun formatCalendarDate(date: LocalDate): String {
    val weekday = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
    val month = when (date.month.number) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        else -> "Dec"
    }
    return "$weekday, ${date.day} $month"
}

@Composable
fun DeliveredStatusIcon(
    tint: Color,
) {
    Box(modifier = Modifier.size(width = 15.dp, height = 12.dp)) {
        Icon(
            imageVector = Heroicons.Solid.Check,
            contentDescription = "Delivered",
            tint = tint,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(12.dp),
        )
        Icon(
            imageVector = Heroicons.Solid.Check,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-1).dp)
                .size(12.dp),
        )
    }
}
