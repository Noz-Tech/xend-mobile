package com.noztek.xend.feature.invites.data.impl

import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.feature.invites.data.local.dao.RelationshipInviteDao
import com.noztek.xend.feature.invites.data.remote.InviteApi
import com.noztek.xend.feature.invites.domain.model.ReceivedInviteModel
import com.noztek.xend.feature.invites.domain.model.SentInviteModel
import com.noztek.xend.feature.invites.domain.repository.RelationshipInviteRepository
import kotlin.time.Instant

class RelationshipInviteRepositoryImpl(
    private val authSessionDao: AuthSessionDao,
    private val inviteApi: InviteApi,
    private val inviteDao: RelationshipInviteDao,
) : RelationshipInviteRepository {
    override suspend fun createInvite(identifier: String, note: String?): String {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val inviteId = inviteApi.createInvite(session.accessToken, identifier, note)
        inviteDao.upsertSentInvite(
            inviteId = inviteId,
            inviterUserId = session.userId,
            inviteeIdentifierOrUserId = identifier,
            note = note,
        )
        return inviteId
    }

    override suspend fun getSentInvites(): List<SentInviteModel> {
        val session = authSessionDao.getCurrentSession() ?: return emptyList()
        runCatching {
            inviteApi.outbox(session.accessToken).forEach { item ->
                inviteDao.upsertSentInviteFromServer(
                    inviteId = item.inviteId,
                    inviterUserId = session.userId,
                    inviteeIdentifierOrUserId = item.inviteeIdentifier,
                    status = item.status,
                    note = item.note,
                    createdAtEpochSeconds = item.createdAt,
                )
            }
        }
        return inviteDao.getSentInvites(session.userId).map {
            SentInviteModel(
                inviteId = it.inviteId,
                inviteeIdentifierOrUserId = it.inviteeIdentifierOrUserId,
                status = it.status,
                note = it.note,
                createdAtEpochSeconds = it.createdAtEpochSeconds,
            )
        }
    }

    override suspend fun getInboxInvites(): List<ReceivedInviteModel> {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        return inviteApi.inbox(session.accessToken).map { item ->
            ReceivedInviteModel(
                inviteId = item.inviteId,
                inviterUserId = item.inviterUserId,
                inviterDisplayName = item.inviterDisplayName,
                inviterIdentifier = item.inviterIdentifier,
                note = item.note,
                status = item.status,
                createdAtEpochSeconds = runCatching { Instant.parse(item.createdAt).epochSeconds }.getOrDefault(0L),
            )
        }
    }

    override suspend fun acceptInvite(inviteId: String) {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        inviteApi.acceptInvite(session.accessToken, inviteId)
    }

    override suspend fun declineInvite(inviteId: String) {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        inviteApi.declineInvite(session.accessToken, inviteId)
    }
}
