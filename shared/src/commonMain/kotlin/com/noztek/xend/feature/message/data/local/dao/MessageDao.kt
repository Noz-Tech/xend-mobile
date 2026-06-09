package com.noztek.xend.feature.message.data.local.dao

import com.noztek.xend.core.time.currentEpochSeconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.noztek.Database

data class MessageLocal(
    val messageId: String,
    val clientTempId: String?,
    val conversationId: String,
    val senderUserId: String,
    val messageType: String,
    val ciphertext: String,
    val replyToMessageId: String?,
    val sentAtEpochSeconds: Long?,
    val serverReceivedAtEpochSeconds: Long?,
    val syncState: String,
    val createdAtEpochSeconds: Long,
    val updatedAtEpochSeconds: Long,
)

class MessageDao(
    private val db: Database,
) {
    @OptIn(ExperimentalUuidApi::class)
    fun insertPendingMessage(
        conversationId: String,
        senderUserId: String,
        messageType: String,
        ciphertext: String,
        replyToMessageId: String?,
    ): String {
        val now = currentEpochSeconds()
        val messageId = Uuid.random().toString()
        db.messagesQueries.insertPendingMessage(
            message_id = messageId,
            client_temp_id = messageId,
            conversation_id = conversationId,
            sender_user_id = senderUserId,
            message_type = messageType,
            ciphertext = ciphertext,
            reply_to_message_id = replyToMessageId,
            sent_at = now,
            server_received_at = null,
            sync_state = "pending",
            created_at = now,
            updated_at = now,
        )
        return messageId
    }

    fun markSent(localMessageId: String, sentAtEpochSeconds: Long = currentEpochSeconds()) {
        db.messagesQueries.markMessageSent(
            sent_at = sentAtEpochSeconds,
            updated_at = sentAtEpochSeconds,
            message_id = localMessageId,
        )
    }

    fun markSynced(localMessageId: String, serverReceivedAtEpochSeconds: Long) {
        db.messagesQueries.markMessageSynced(
            server_received_at = serverReceivedAtEpochSeconds,
            updated_at = currentEpochSeconds(),
            message_id = localMessageId,
        )
    }

    fun reconcileSentMessageIdentity(
        clientTempId: String,
        serverMessageId: String,
        serverReceivedAtEpochSeconds: Long,
    ) {
        val now = currentEpochSeconds()
        db.messagesQueries.reconcileSentMessageIdentity(
            message_id = serverMessageId,
            server_received_at = serverReceivedAtEpochSeconds,
            updated_at = now,
            client_temp_id = clientTempId,
        )
        db.messagesQueries.relinkReplyTargets(
            reply_to_message_id = serverMessageId,
            updated_at = now,
            reply_to_message_id_ = clientTempId,
        )
    }

    fun markFailed(localMessageId: String) {
        db.messagesQueries.markMessageFailed(
            updated_at = currentEpochSeconds(),
            message_id = localMessageId,
        )
    }

    fun markPending(localMessageId: String) {
        db.messagesQueries.markMessagePending(
            updated_at = currentEpochSeconds(),
            message_id = localMessageId,
        )
    }

    fun upsertReceivedMessage(
        messageId: String,
        clientTempId: String?,
        conversationId: String,
        senderUserId: String,
        messageType: String,
        ciphertext: String,
        replyToMessageId: String?,
        sentAtEpochSeconds: Long?,
        serverReceivedAtEpochSeconds: Long,
        syncState: String = "synced",
    ) {
        val createdAt = sentAtEpochSeconds ?: serverReceivedAtEpochSeconds
        db.messagesQueries.upsertReceivedMessage(
            message_id = messageId,
            client_temp_id = clientTempId,
            conversation_id = conversationId,
            sender_user_id = senderUserId,
            message_type = messageType,
            ciphertext = ciphertext,
            reply_to_message_id = replyToMessageId,
            sent_at = sentAtEpochSeconds,
            server_received_at = serverReceivedAtEpochSeconds,
            sync_state = syncState,
            created_at = createdAt,
            updated_at = serverReceivedAtEpochSeconds,
        )
    }

    fun getPendingOrFailed(): List<MessageLocal> =
        db.messagesQueries.selectPendingOrFailedMessages().executeAsList().map(::toLocal)

    fun getMessagesByConversation(conversationId: String): List<MessageLocal> =
        db.messagesQueries.selectMessagesByConversation(conversationId).executeAsList().map(::toLocal)

    fun exists(messageId: String): Boolean =
        db.messagesQueries.selectMessageById(messageId).executeAsOneOrNull() != null

    fun getById(messageId: String): MessageLocal? =
        db.messagesQueries.selectMessageById(messageId).executeAsOneOrNull()?.let(::toLocal)

    fun getIncomingMessageIdsByConversation(conversationId: String, currentUserId: String): List<String> =
        db.messagesQueries.selectIncomingMessageIdsByConversation(conversationId, currentUserId).executeAsList()

    fun countUnreadIncomingByConversation(conversationId: String, currentUserId: String): Int =
        db.messagesQueries.countUnreadIncomingByConversation(
            conversation_id = conversationId,
            sender_user_id = currentUserId,
            user_id = currentUserId,
        ).executeAsOne().toInt()

    fun getLatestServerReceivedAt(): Long? =
        db.messagesQueries.selectLatestServerReceivedAt().executeAsOneOrNull()?.latest_server_received_at

    private fun toLocal(row: org.noztek.Messages): MessageLocal {
        return MessageLocal(
            messageId = row.message_id,
            clientTempId = row.client_temp_id,
            conversationId = row.conversation_id,
            senderUserId = row.sender_user_id,
            messageType = row.message_type,
            ciphertext = row.ciphertext,
            replyToMessageId = row.reply_to_message_id,
            sentAtEpochSeconds = row.sent_at,
            serverReceivedAtEpochSeconds = row.server_received_at,
            syncState = row.sync_state,
            createdAtEpochSeconds = row.created_at,
            updatedAtEpochSeconds = row.updated_at,
        )
    }
}
