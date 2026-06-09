package com.noztek.xend.feature.message.domain.usecase

import com.noztek.xend.core.realtime.PresenceGateway
import com.noztek.xend.core.realtime.RealtimeSessionCoordinator
import com.noztek.xend.feature.device.domain.usecase.DeviceKeysSyncer
import com.noztek.xend.feature.message.domain.model.ChatConversationHeaderModel
import com.noztek.xend.feature.message.domain.model.ChatMessageModel
import kotlin.time.Clock
import kotlin.time.Instant

data class ConversationEntryModel(
    val currentUserId: String?,
    val header: ChatConversationHeaderModel,
    val messages: List<ChatMessageModel>,
    val presenceLabel: String?,
)

class LoadConversationEntryUseCase(
    private val getConversationHeader: GetConversationHeaderUseCase,
    private val getConversationMessages: GetConversationMessagesUseCase,
    private val getCurrentChatSession: GetCurrentChatSessionUseCase,
    private val markConversationRead: MarkConversationReadUseCase,
    private val presenceGateway: PresenceGateway,
    private val deviceKeysSyncer: DeviceKeysSyncer,
    private val syncMessages: SyncMessagesUseCase,
) {
    suspend operator fun invoke(conversationId: String): ConversationEntryModel {
        val session = getCurrentChatSession()
        if (session != null) {
            deviceKeysSyncer.syncIfNeeded(session.accessToken, session.deviceId)
        }
        syncMessages()
        markConversationRead(conversationId)
        syncMessages()

        val header = getConversationHeader(conversationId)
        val messages = getConversationMessages(conversationId)
        val presenceLabel = loadPresenceLabel(
            currentUserId = session?.userId,
            accessToken = session?.accessToken,
            partnerUserId = header.partnerUserId,
        )

        return ConversationEntryModel(
            currentUserId = session?.userId,
            header = header,
            messages = messages,
            presenceLabel = presenceLabel,
        )
    }

    private suspend fun loadPresenceLabel(
        currentUserId: String?,
        accessToken: String?,
        partnerUserId: String?,
    ): String? {
        if (currentUserId.isNullOrBlank() || accessToken.isNullOrBlank() || partnerUserId.isNullOrBlank()) return null
        val presence = presenceGateway.getUserPresence(accessToken, partnerUserId)
        return formatPresenceLabel(presence.isOnline, presence.lastSeenEpochSeconds)
    }

    private fun formatPresenceLabel(isOnline: Boolean, lastSeenEpochSeconds: Long?): String {
        if (isOnline) return "online"
        if (lastSeenEpochSeconds == null) return "offline"

        val now = Clock.System.now().epochSeconds
        val deltaSeconds = (now - lastSeenEpochSeconds).coerceAtLeast(0)
        return when {
            deltaSeconds < 60 -> "last seen just now"
            deltaSeconds < 3600 -> "last seen ${deltaSeconds / 60}m ago"
            deltaSeconds < 86400 -> "last seen ${deltaSeconds / 3600}h ago"
            else -> {
                val day = Instant.fromEpochSeconds(lastSeenEpochSeconds).toString().substring(0, 10)
                "last seen $day"
            }
        }
    }
}

class SendConversationTypingUseCase(
    private val realtimeSessionCoordinator: RealtimeSessionCoordinator,
) {
    operator fun invoke(conversationId: String, isTyping: Boolean) {
        realtimeSessionCoordinator.sendTyping(conversationId, isTyping)
    }
}

class SendConversationMessageUseCase(
    private val getConversationMessages: GetConversationMessagesUseCase,
    private val sendSecureTextMessage: SendSecureTextMessageUseCase,
    private val syncMessages: SyncMessagesUseCase,
) {
    suspend operator fun invoke(
        conversationId: String,
        text: String,
        replyToMessageId: String?,
    ): List<ChatMessageModel> {
        sendSecureTextMessage(conversationId, text, replyToMessageId)
        syncMessages()
        return getConversationMessages(conversationId)
    }
}

class RetryConversationMessageUseCase(
    private val getConversationMessages: GetConversationMessagesUseCase,
    private val retryFailedMessage: RetryFailedMessageUseCase,
    private val syncMessages: SyncMessagesUseCase,
) {
    suspend operator fun invoke(conversationId: String, messageId: String): List<ChatMessageModel> {
        retryFailedMessage(messageId)
        syncMessages()
        return getConversationMessages(conversationId)
    }
}

class ToggleConversationReactionUseCase(
    private val toggleMessageReaction: ToggleMessageReactionUseCase,
) {
    suspend operator fun invoke(messageId: String, emoji: String, remove: Boolean) {
        toggleMessageReaction(messageId, emoji, remove)
    }
}
