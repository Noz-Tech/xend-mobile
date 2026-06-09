package com.noztek.xend.feature.invites.data.local.dao

import com.noztek.xend.core.time.currentEpochSeconds
import org.noztek.Database

data class SentInviteLocal(
    val inviteId: String,
    val inviteeIdentifierOrUserId: String,
    val status: String,
    val note: String?,
    val createdAtEpochSeconds: Long,
)

class RelationshipInviteDao(
    private val db: Database,
) {
    fun upsertSentInvite(
        inviteId: String,
        inviterUserId: String,
        inviteeIdentifierOrUserId: String,
        note: String?,
    ) {
        val now = currentEpochSeconds()
        db.relationshipInvitesQueries.upsertRelationshipInvite(
            invite_id = inviteId,
            relationship_space_id = null,
            inviter_user_id = inviterUserId,
            invitee_user_id = inviteeIdentifierOrUserId,
            status = "pending",
            note = note,
            expires_at = null,
            responded_at = null,
            created_at = now,
            updated_at = now,
            sync_state = "synced",
        )
    }

    fun getSentInvites(inviterUserId: String): List<SentInviteLocal> =
        db.relationshipInvitesQueries.selectInviteOutboxForUser(inviterUserId).executeAsList().map {
            SentInviteLocal(
                inviteId = it.invite_id,
                inviteeIdentifierOrUserId = it.invitee_user_id,
                status = it.status,
                note = it.note,
                createdAtEpochSeconds = it.created_at,
            )
        }

    fun upsertSentInviteFromServer(
        inviteId: String,
        inviterUserId: String,
        inviteeIdentifierOrUserId: String,
        status: String,
        note: String?,
        createdAtEpochSeconds: Long,
    ) {
        db.relationshipInvitesQueries.upsertRelationshipInvite(
            invite_id = inviteId,
            relationship_space_id = null,
            inviter_user_id = inviterUserId,
            invitee_user_id = inviteeIdentifierOrUserId,
            status = status,
            note = note,
            expires_at = null,
            responded_at = null,
            created_at = createdAtEpochSeconds,
            updated_at = currentEpochSeconds(),
            sync_state = "synced",
        )
    }
}
