package com.noztek.xend.feature.invites.domain.model

data class SentInviteModel(
    val inviteId: String,
    val inviteeIdentifierOrUserId: String,
    val status: String,
    val note: String?,
    val createdAtEpochSeconds: Long,
)
