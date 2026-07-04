package com.noztek.xend.feature.incominginvite.domain.model

data class IncomingInviteDetailsModel(
    val inviteId: String,
    val inviterDisplayName: String,
    val inviterIdentifier: String,
    val note: String?,
    val hasRelationshipSpace: Boolean,
)
