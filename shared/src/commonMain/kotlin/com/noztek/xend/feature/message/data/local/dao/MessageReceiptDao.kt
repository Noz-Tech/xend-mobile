package com.noztek.xend.feature.message.data.local.dao

import org.noztek.Database

data class MessageReceiptLocal(
    val messageId: String,
    val userId: String,
    val status: String,
    val updatedAtEpochSeconds: Long,
)

class MessageReceiptDao(
    private val db: Database,
) {
    fun upsert(
        messageId: String,
        userId: String,
        status: String,
        updatedAtEpochSeconds: Long,
    ) {
        db.messageReceiptsQueries.upsertMessageReceipt(
            message_id = messageId,
            user_id = userId,
            status = status,
            updated_at = updatedAtEpochSeconds,
        )
    }

    fun getByMessageIds(messageIds: Collection<String>): Map<String, MessageReceiptLocal> {
        if (messageIds.isEmpty()) return emptyMap()
        return db.messageReceiptsQueries
            .selectReceiptsForMessageIds(messageIds.toList())
            .executeAsList()
            .associateBy(
                keySelector = { it.message_id },
                valueTransform = {
                    MessageReceiptLocal(
                        messageId = it.message_id,
                        userId = it.user_id,
                        status = it.status,
                        updatedAtEpochSeconds = it.updated_at,
                    )
                },
            )
    }

    fun markMessagesRead(messageIds: Collection<String>, userId: String, updatedAtEpochSeconds: Long) {
        messageIds.forEach { messageId ->
            upsert(
                messageId = messageId,
                userId = userId,
                status = "read",
                updatedAtEpochSeconds = updatedAtEpochSeconds,
            )
        }
    }
}
