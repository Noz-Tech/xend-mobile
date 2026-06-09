package com.noztek.xend.feature.message.domain.model

data class ChatMessageModel(
    val messageId: String,
    val conversationId: String,
    val senderUserId: String,
    val body: String,
    val replyToMessageId: String? = null,
    val replyPreviewText: String? = null,
    val syncState: String,
    val receiptStatus: String? = null,
    val receiptUpdatedAtEpochSeconds: Long? = null,
    val sentAtEpochSeconds: Long?,
    val createdAtEpochSeconds: Long,
    val reactions: List<ReactionModel> = emptyList(),
)

data class ReactionModel(
    val emoji: String,
    val userId: String,
    val removed: Boolean = false,
)
