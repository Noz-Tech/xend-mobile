package com.noztek.xend.feature.incominginvite.domain.usecase

import com.noztek.xend.feature.incominginvite.domain.model.IncomingInviteDetailsModel
import com.noztek.xend.feature.invites.domain.usecase.DeclineRelationshipInviteUseCase
import com.noztek.xend.feature.invites.domain.usecase.GetInboxInvitesUseCase
import com.noztek.xend.feature.invites.domain.usecase.RelationshipInviteAcceptanceCompleter
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase

class LoadIncomingInviteUseCase(
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getInboxInvites: GetInboxInvitesUseCase,
) {
    suspend operator fun invoke(): IncomingInviteDetailsModel? {
        runCatching { syncRelationshipSpaces() }
        val hasRelationshipSpace = getDefaultRelationshipSpace() != null
        val invite = runCatching { getInboxInvites() }
            .getOrDefault(emptyList())
            .firstOrNull { it.status.equals("pending", ignoreCase = true) }
            ?: return null

        return IncomingInviteDetailsModel(
            inviteId = invite.inviteId,
            inviterDisplayName = invite.inviterDisplayName,
            inviterIdentifier = invite.inviterIdentifier,
            note = invite.note,
            hasRelationshipSpace = hasRelationshipSpace,
        )
    }
}

class AcceptIncomingInviteUseCase(
    private val acceptanceCompleter: RelationshipInviteAcceptanceCompleter,
) {
    suspend operator fun invoke(inviteId: String) {
        acceptanceCompleter.accept(inviteId)
    }
}

class DeclineIncomingInviteUseCase(
    private val declineRelationshipInvite: DeclineRelationshipInviteUseCase,
) {
    suspend operator fun invoke(inviteId: String) {
        declineRelationshipInvite(inviteId)
    }
}
