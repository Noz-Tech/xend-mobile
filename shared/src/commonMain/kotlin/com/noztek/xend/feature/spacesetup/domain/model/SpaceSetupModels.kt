package com.noztek.xend.feature.spacesetup.domain.model

enum class AuthenticatedEntryDestination {
    MAIN,
    INCOMING_INVITE,
    OUTGOING_INVITE,
    SPACE_SETUP,
}

data class SpaceSetupSnapshotModel(
    val ownIdentifier: String,
    val displayName: String,
    val hasRelationshipSpace: Boolean,
    val pendingIncomingInvites: Int,
    val pendingSentInvites: Int,
)
