package com.noztek.xend.feature.invites.domain.model

data class ReceivedInviteModel(
    val inviteId: String,
    val inviterUserId: String,
    val inviterDisplayName: String,
    val inviterIdentifier: String,
    val note: String?,
    val status: String,
    val createdAtEpochSeconds: Long,
)
