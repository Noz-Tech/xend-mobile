package com.noztek.xend.feature.message.domain.usecase

import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import com.noztek.xend.feature.message.domain.model.ChatConversationHeaderModel
import com.noztek.xend.feature.message.domain.model.ChatMessageModel
import com.noztek.xend.feature.message.domain.repository.MessageRepository

class SendSecureTextMessageUseCase(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke(conversationId: String, plaintext: String, replyToMessageId: String? = null) {
        repository.sendSecureTextMessage(conversationId, plaintext, replyToMessageId)
    }
}

class RetryFailedMessageUseCase(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke(messageId: String) {
        repository.retryFailedMessage(messageId)
    }
}

class SyncMessagesUseCase(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke() {
        repository.syncMessages()
    }
}

class MarkConversationReadUseCase(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke(conversationId: String) {
        repository.markConversationRead(conversationId)
    }
}

class GetUnreadCountUseCase(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke(conversationId: String): Int = repository.getUnreadCount(conversationId)
}

class GetConversationHeaderUseCase(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke(conversationId: String): ChatConversationHeaderModel =
        repository.getConversationHeader(conversationId)
}

class GetConversationMessagesUseCase(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke(conversationId: String): List<ChatMessageModel> =
        repository.getConversationMessages(conversationId)
}

class GetCurrentChatSessionUseCase(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke(): AuthSessionModel? = repository.getCurrentSession()
}

class ToggleMessageReactionUseCase(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke(messageId: String, emoji: String, remove: Boolean) {
        repository.toggleReaction(messageId, emoji, remove)
    }
}
