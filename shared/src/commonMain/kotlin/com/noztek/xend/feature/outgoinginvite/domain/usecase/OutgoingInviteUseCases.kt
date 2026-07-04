package com.noztek.xend.feature.outgoinginvite.domain.usecase

import com.noztek.xend.feature.invites.domain.usecase.GetSentInvitesUseCase
import com.noztek.xend.feature.outgoinginvite.domain.model.OutgoingInviteDetailsModel
import com.noztek.xend.feature.outgoinginvite.domain.model.OutgoingInviteSnapshotModel
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase

class LoadOutgoingInviteUseCase(
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getSentInvites: GetSentInvitesUseCase,
) {
    suspend operator fun invoke(): OutgoingInviteSnapshotModel {
        runCatching { syncRelationshipSpaces() }
        val hasRelationshipSpace = getDefaultRelationshipSpace() != null
        val invite = runCatching { getSentInvites() }
            .getOrDefault(emptyList())
            .firstOrNull { it.status.equals("pending", ignoreCase = true) }

        return OutgoingInviteSnapshotModel(
            invite = invite?.let {
                OutgoingInviteDetailsModel(
                    inviteId = it.inviteId,
                    inviteeIdentifier = it.inviteeIdentifierOrUserId,
                    note = it.note,
                )
            },
            hasRelationshipSpace = hasRelationshipSpace,
        )
    }
}
