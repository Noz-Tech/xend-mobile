package com.noztek.xend.feature.invites.domain.usecase

import com.noztek.xend.feature.invites.domain.model.ReceivedInviteModel
import com.noztek.xend.feature.invites.domain.model.SentInviteModel
import com.noztek.xend.feature.invites.domain.repository.RelationshipInviteRepository

class CreateRelationshipInviteUseCase(
    private val repository: RelationshipInviteRepository,
) {
    suspend operator fun invoke(identifier: String, note: String?): String {
        return repository.createInvite(identifier.trim(), note?.trim()?.ifBlank { null })
    }
}

class AcceptRelationshipInviteUseCase(
    private val repository: RelationshipInviteRepository,
) {
    suspend operator fun invoke(inviteId: String) = repository.acceptInvite(inviteId)
}

class DeclineRelationshipInviteUseCase(
    private val repository: RelationshipInviteRepository,
) {
    suspend operator fun invoke(inviteId: String) = repository.declineInvite(inviteId)
}

class GetInboxInvitesUseCase(
    private val repository: RelationshipInviteRepository,
) {
    suspend operator fun invoke(): List<ReceivedInviteModel> = repository.getInboxInvites()
}

class GetSentInvitesUseCase(
    private val repository: RelationshipInviteRepository,
) {
    suspend operator fun invoke(): List<SentInviteModel> = repository.getSentInvites()
}
