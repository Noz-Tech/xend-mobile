package com.noztek.xend.feature.invites.domain.usecase

import com.noztek.xend.feature.invites.domain.model.SentInviteModel

data class RelationshipInviteSubmissionResult(
    val inviteId: String,
    val sentInvites: List<SentInviteModel>,
)

class RelationshipInviteSubmissionUseCase(
    private val createRelationshipInvite: CreateRelationshipInviteUseCase,
    private val getSentInvites: GetSentInvitesUseCase,
) {
    suspend operator fun invoke(identifier: String, note: String?): RelationshipInviteSubmissionResult {
        val inviteId = createRelationshipInvite(identifier, note)
        return RelationshipInviteSubmissionResult(
            inviteId = inviteId,
            sentInvites = getSentInvites(),
        )
    }
}
