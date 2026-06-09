package com.noztek.xend.feature.message.presentation.state

import com.noztek.xend.feature.message.domain.model.ChatConversationHeaderModel
import com.noztek.xend.feature.message.domain.model.ChatMessageModel

data class MessageUiState(
    val isLoading: Boolean = true,
    val currentUserId: String? = null,
    val header: ChatConversationHeaderModel? = null,
    val items: List<ChatMessageModel> = emptyList(),
    val message: String? = null,
    val sending: Boolean = false,
    val retryingMessageId: String? = null,
    val presenceLabel: String? = null,
    val isTyping: Boolean = false,
)
