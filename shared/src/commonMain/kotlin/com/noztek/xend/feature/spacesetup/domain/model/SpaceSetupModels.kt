package com.noztek.xend.feature.spacesetup.domain.model

enum class AuthenticatedEntryDestination {
    MAIN,
    SPACE_SETUP,
}

data class SpaceSetupSnapshotModel(
    val ownIdentifier: String,
    val displayName: String,
    val hasRelationshipSpace: Boolean,
    val pendingIncomingInvites: Int,
    val pendingSentInvites: Int,
)
