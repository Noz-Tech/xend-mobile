package com.noztek.xend.feature.outgoinginvite.domain.model

data class OutgoingInviteDetailsModel(
    val inviteId: String,
    val inviteeIdentifier: String,
    val note: String?,
)

data class OutgoingInviteSnapshotModel(
    val invite: OutgoingInviteDetailsModel?,
    val hasRelationshipSpace: Boolean,
)
