package com.noztek.xend.feature.message.domain.repository

import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import com.noztek.xend.feature.message.domain.model.ChatConversationHeaderModel
import com.noztek.xend.feature.message.domain.model.ChatMessageModel

interface MessageRepository {
    suspend fun sendSecureTextMessage(conversationId: String, plaintext: String, replyToMessageId: String? = null)
    suspend fun retryFailedMessage(messageId: String)
    suspend fun syncMessages()
    suspend fun markConversationRead(conversationId: String)
    suspend fun getUnreadCount(conversationId: String): Int
    suspend fun getConversationHeader(conversationId: String): ChatConversationHeaderModel
    suspend fun getConversationMessages(conversationId: String): List<ChatMessageModel>
    suspend fun getCurrentSession(): AuthSessionModel?
    suspend fun toggleReaction(messageId: String, emoji: String, remove: Boolean)
}
