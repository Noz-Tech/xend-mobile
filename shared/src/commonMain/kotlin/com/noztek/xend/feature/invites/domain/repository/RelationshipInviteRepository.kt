package com.noztek.xend.feature.invites.domain.repository

import com.noztek.xend.feature.invites.domain.model.ReceivedInviteModel
import com.noztek.xend.feature.invites.domain.model.SentInviteModel

interface RelationshipInviteRepository {
    suspend fun createInvite(identifier: String, note: String?): String
    suspend fun getSentInvites(): List<SentInviteModel>
    suspend fun getInboxInvites(): List<ReceivedInviteModel>
    suspend fun acceptInvite(inviteId: String)
    suspend fun declineInvite(inviteId: String)
}
