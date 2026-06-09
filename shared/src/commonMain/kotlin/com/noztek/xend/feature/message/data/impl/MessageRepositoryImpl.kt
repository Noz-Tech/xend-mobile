package com.noztek.xend.feature.message.data.impl

import com.noztek.xend.core.time.currentEpochSeconds
import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import com.noztek.xend.feature.device.data.local.dao.SignalSessionDao
import com.noztek.xend.feature.device.data.remote.DeviceApi
import com.noztek.xend.feature.device.domain.usecase.SignalSessionBootstrapper
import com.noztek.xend.feature.message.data.crypto.DuplicateSignalMessageException
import com.noztek.xend.feature.message.data.crypto.SecureMessageCipher
import com.noztek.xend.feature.message.data.local.dao.ConversationDao
import com.noztek.xend.feature.message.data.local.dao.MessageDao
import com.noztek.xend.feature.message.data.local.dao.MessageReactionDao
import com.noztek.xend.feature.message.data.local.dao.MessageReceiptDao
import com.noztek.xend.feature.message.data.remote.MessageApi
import com.noztek.xend.feature.message.data.remote.MessageDto
import com.noztek.xend.feature.message.data.remote.SendMessageRequestDto
import com.noztek.xend.feature.message.domain.model.ChatConversationHeaderModel
import com.noztek.xend.feature.message.domain.model.ChatMessageModel
import com.noztek.xend.feature.message.domain.model.ReactionModel
import com.noztek.xend.feature.message.domain.repository.MessageRepository
import com.noztek.xend.feature.space.data.remote.SpaceApi

class MessageRepositoryImpl(
    private val authSessionDao: AuthSessionDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val messageReceiptDao: MessageReceiptDao,
    private val messageReactionDao: MessageReactionDao,
    private val messageApi: MessageApi,
    private val deviceApi: DeviceApi,
    private val spaceApi: SpaceApi,
    private val signalSessionDao: SignalSessionDao,
    private val signalMessageCipher: SecureMessageCipher,
    private val signalSessionBootstrapper: SignalSessionBootstrapper,
) : MessageRepository {
    override suspend fun getConversationHeader(conversationId: String): ChatConversationHeaderModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val conversation = requireNotNull(conversationDao.getById(conversationId)) { "Conversation not found locally" }
        val partner = spaceApi.getSpaceMembers(session.accessToken, conversation.relationshipSpaceId)
            .firstOrNull { it.userId != session.userId }

        return ChatConversationHeaderModel(
            title = partner?.displayName ?: "Messages",
            subtitle = partner?.identifier,
            partnerUserId = partner?.userId,
        )
    }

    override suspend fun getConversationMessages(conversationId: String): List<ChatMessageModel> {
        val messages = messageDao.getMessagesByConversation(conversationId)
        val receipts = messageReceiptDao.getByMessageIds(messages.map { it.messageId })
        val byId = messages.associateBy { it.messageId }
        val reactionsByMessage = messageReactionDao.getByMessageIds(messages.map { it.messageId })

        return messages.map { message ->
            val receipt = receipts[message.messageId]
            ChatMessageModel(
                messageId = message.messageId,
                conversationId = message.conversationId,
                senderUserId = message.senderUserId,
                body = message.ciphertext,
                replyToMessageId = message.replyToMessageId,
                replyPreviewText = message.replyToMessageId?.let { replyId -> byId[replyId]?.ciphertext },
                syncState = message.syncState,
                receiptStatus = receipt?.status,
                receiptUpdatedAtEpochSeconds = receipt?.updatedAtEpochSeconds,
                sentAtEpochSeconds = message.sentAtEpochSeconds,
                createdAtEpochSeconds = message.createdAtEpochSeconds,
                reactions = reactionsByMessage[message.messageId].orEmpty()
                    .map { reaction ->
                        ReactionModel(
                            emoji = reaction.emoji,
                            userId = reaction.userId,
                            removed = reaction.removedAtEpochSeconds != null,
                        )
                    }
                    .filterNot { it.removed },
            )
        }
    }

    override suspend fun getCurrentSession(): AuthSessionModel? = authSessionDao.getCurrentSession()

    override suspend fun toggleReaction(messageId: String, emoji: String, remove: Boolean) {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        if (remove) {
            messageApi.removeReaction(session.accessToken, messageId, emoji)
        } else {
            messageApi.addReaction(session.accessToken, messageId, emoji)
        }
    }

    override suspend fun sendSecureTextMessage(
        conversationId: String,
        plaintext: String,
        replyToMessageId: String?,
    ) {
        sendSecureTextMessageInternal(
            conversationId = conversationId,
            plaintext = plaintext,
            localMessageId = null,
            replyToMessageId = replyToMessageId,
        )
    }

    override suspend fun retryFailedMessage(messageId: String) {
        val local = requireNotNull(messageDao.getById(messageId)) { "Message not found" }
        messageDao.markPending(local.messageId)
        sendSecureTextMessageInternal(
            conversationId = local.conversationId,
            plaintext = local.ciphertext,
            localMessageId = local.messageId,
            replyToMessageId = local.replyToMessageId,
        )
    }

    override suspend fun syncMessages() {
        val session = authSessionDao.getCurrentSession() ?: return
        signalSessionBootstrapper.bootstrap()
        val since = messageDao.getLatestServerReceivedAt()
        val items = messageApi.syncMessages(session.accessToken, since)

        items.forEach { item ->
            upsertReceipt(item)
            item.reactions.forEach { reaction ->
                messageReactionDao.upsert(
                    messageId = reaction.messageId,
                    userId = reaction.userId,
                    emoji = reaction.emoji,
                    removedAtEpochSeconds = reaction.removedAt,
                    updatedAtEpochSeconds = reaction.updatedAt,
                )
            }
            if (messageDao.exists(item.messageId)) return@forEach

            if (item.senderUserId == session.userId) {
                messageDao.reconcileSentMessageIdentity(
                    clientTempId = item.clientMessageId,
                    serverMessageId = item.messageId,
                    serverReceivedAtEpochSeconds = item.createdAt,
                )
                return@forEach
            }

            val remoteSession = signalSessionDao.getUsableDeterministicSession(item.senderUserId, item.senderDeviceId)
                ?: return@forEach
            val plaintext = runCatching {
                signalMessageCipher.decryptFromMessage(
                    senderUserId = item.senderUserId,
                    senderDeviceId = item.senderDeviceId,
                    senderRegistrationId = remoteSession.registrationId,
                    messageType = item.messageType,
                    ciphertextBase64 = item.ciphertext,
                )
            }.getOrElse { error ->
                if (error is DuplicateSignalMessageException) return@forEach
                signalSessionDao.markSessionResetRequired(
                    userId = remoteSession.userId,
                    deviceId = remoteSession.deviceId,
                    staleReason = SignalSessionDao.STALE_REASON_DECRYPT_FAILED,
                )
                throw error
            }

            messageDao.upsertReceivedMessage(
                messageId = item.messageId,
                clientTempId = item.clientMessageId,
                conversationId = item.conversationId,
                senderUserId = item.senderUserId,
                messageType = "text",
                ciphertext = plaintext,
                replyToMessageId = item.replyToMessageId,
                sentAtEpochSeconds = item.senderTimestamp,
                serverReceivedAtEpochSeconds = item.createdAt,
            )
        }
    }

    override suspend fun markConversationRead(conversationId: String) {
        val session = authSessionDao.getCurrentSession() ?: return
        val now = currentEpochSeconds()
        val incomingMessageIds = messageDao.getIncomingMessageIdsByConversation(conversationId, session.userId)
        messageReceiptDao.markMessagesRead(incomingMessageIds, session.userId, now)
        messageApi.markConversationRead(session.accessToken, conversationId)
    }

    override suspend fun getUnreadCount(conversationId: String): Int {
        val session = authSessionDao.getCurrentSession() ?: return 0
        return messageDao.countUnreadIncomingByConversation(conversationId, session.userId)
    }

    private suspend fun sendSecureTextMessageInternal(
        conversationId: String,
        plaintext: String,
        localMessageId: String?,
        replyToMessageId: String?,
    ) {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val conversation = requireNotNull(conversationDao.getById(conversationId)) { "Conversation not found locally" }
        val members = spaceApi.getSpaceMembers(session.accessToken, conversation.relationshipSpaceId)
            .filter { it.userId != session.userId }

        require(members.size == 1) { "Secure messaging currently supports one recipient per conversation" }

        val recipientUserId = members.first().userId
        val recipientBundle = deviceApi.getRecipientPrekeys(session.accessToken, recipientUserId)
            .devices
            .firstOrNull()
            ?: error("Recipient prekeys not found")
        val targetSession = ensureRecipientSession(recipientUserId)

        val clientMessageId = localMessageId ?: messageDao.insertPendingMessage(
            conversationId = conversationId,
            senderUserId = session.userId,
            messageType = "text",
            ciphertext = plaintext,
            replyToMessageId = replyToMessageId,
        )

        try {
            ensureTrustedIdentity(targetSession, recipientBundle.identityKeyPublic)
            val envelope = signalMessageCipher.encryptForSession(targetSession, plaintext)
            messageDao.markSent(clientMessageId)

            val response = messageApi.sendMessage(
                accessToken = session.accessToken,
                conversationId = conversationId,
                request = SendMessageRequestDto(
                    clientMessageId = clientMessageId,
                    messageType = envelope.messageType,
                    ciphertext = envelope.ciphertextBase64,
                    replyToMessageId = replyToMessageId,
                    senderTimestamp = currentEpochSeconds(),
                ),
            )
            messageDao.reconcileSentMessageIdentity(
                clientTempId = clientMessageId,
                serverMessageId = response.messageId,
                serverReceivedAtEpochSeconds = response.createdAt,
            )
            upsertReceipt(response)
        } catch (error: Exception) {
            signalSessionDao.markSessionResetRequired(
                userId = targetSession.userId,
                deviceId = targetSession.deviceId,
                staleReason = SignalSessionDao.STALE_REASON_SEND_FAILED,
            )
            messageDao.markFailed(clientMessageId)
            throw error
        }
    }

    private fun upsertReceipt(item: MessageDto) {
        val receiptUserId = item.receiptUserId ?: return
        val receiptStatus = item.receiptStatus ?: return
        val updatedAt = item.readAt ?: item.deliveredAt ?: item.createdAt
        messageReceiptDao.upsert(
            messageId = item.messageId,
            userId = receiptUserId,
            status = receiptStatus,
            updatedAtEpochSeconds = updatedAt,
        )
    }

    private suspend fun ensureRecipientSession(recipientUserId: String): SignalSessionDao.SignalSessionLocal {
        val existing = signalSessionDao.getSessionsForUser(recipientUserId)
            .firstOrNull { session ->
                session.deviceId.isNotBlank() &&
                    signalSessionDao.getUsableDeterministicSession(session.userId, session.deviceId) != null
            }
        if (existing != null) return existing

        signalSessionBootstrapper.bootstrap(listOf(recipientUserId))

        return signalSessionDao.getSessionsForUser(recipientUserId)
            .firstOrNull { session ->
                session.deviceId.isNotBlank() &&
                    signalSessionDao.getUsableDeterministicSession(session.userId, session.deviceId) != null
            }
            ?: error("No bootstrapped signal session for recipient")
    }

    private fun ensureTrustedIdentity(
        session: SignalSessionDao.SignalSessionLocal,
        currentIdentityKeyPublic: String,
    ) {
        val currentIdentity = signalSessionDao.getRemoteIdentity(session.userId, session.deviceId)
        if (currentIdentity?.identityKeyPublic != currentIdentityKeyPublic) {
            signalSessionDao.markSessionResetRequired(
                userId = session.userId,
                deviceId = session.deviceId,
                sessionState = "identity_changed",
                staleReason = SignalSessionDao.STALE_REASON_REMOTE_IDENTITY_CHANGED,
            )
            error("Recipient identity changed. Reset trust before sending.")
        }
    }
}
