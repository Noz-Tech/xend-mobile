package com.noztek.xend.feature.message.data.local.dao

import org.noztek.Database

data class MessageReactionLocal(
    val messageId: String,
    val userId: String,
    val emoji: String,
    val removedAtEpochSeconds: Long?,
    val updatedAtEpochSeconds: Long,
)

class MessageReactionDao(
    private val db: Database,
) {
    fun upsert(messageId: String, userId: String, emoji: String, removedAtEpochSeconds: Long?, updatedAtEpochSeconds: Long) {
        db.messageReactionsQueries.upsertMessageReaction(
            message_id = messageId,
            user_id = userId,
            emoji = emoji,
            removed_at = removedAtEpochSeconds,
            updated_at = updatedAtEpochSeconds,
        )
    }

    fun getByMessageIds(messageIds: List<String>): Map<String, List<MessageReactionLocal>> {
        if (messageIds.isEmpty()) return emptyMap()
        return db.messageReactionsQueries.selectReactionsByMessageIds(messageIds)
            .executeAsList()
            .map {
                MessageReactionLocal(
                    messageId = it.message_id,
                    userId = it.user_id,
                    emoji = it.emoji,
                    removedAtEpochSeconds = it.removed_at,
                    updatedAtEpochSeconds = it.updated_at,
                )
            }
            .groupBy { it.messageId }
    }

    fun clearRemovedByMessageIds(messageIds: List<String>) {
        if (messageIds.isEmpty()) return
        db.messageReactionsQueries.clearRemovedReactionsByMessageIds(messageIds)
    }
}
