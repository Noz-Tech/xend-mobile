package com.noztek.xend.feature.spacesetup.domain.usecase

import com.noztek.xend.feature.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.noztek.xend.feature.invites.domain.usecase.GetInboxInvitesUseCase
import com.noztek.xend.feature.invites.domain.usecase.GetSentInvitesUseCase
import com.noztek.xend.feature.invites.domain.usecase.RelationshipInviteSubmissionResult
import com.noztek.xend.feature.invites.domain.usecase.RelationshipInviteSubmissionUseCase
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase
import com.noztek.xend.feature.spacesetup.domain.model.AuthenticatedEntryDestination
import com.noztek.xend.feature.spacesetup.domain.model.SpaceSetupSnapshotModel

class ResolveAuthenticatedEntryDestinationUseCase(
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getInboxInvites: GetInboxInvitesUseCase,
    private val getSentInvites: GetSentInvitesUseCase,
) {
    suspend operator fun invoke(): AuthenticatedEntryDestination {
        runCatching { syncRelationshipSpaces() }
        if (getDefaultRelationshipSpace() != null) return AuthenticatedEntryDestination.MAIN

        val hasPendingIncomingInvite = runCatching { getInboxInvites() }
            .getOrDefault(emptyList())
            .any { it.status.equals("pending", ignoreCase = true) }

        return if (hasPendingIncomingInvite) {
            AuthenticatedEntryDestination.INCOMING_INVITE
        } else {
            val hasPendingSentInvite = runCatching { getSentInvites() }
                .getOrDefault(emptyList())
                .any { it.status.equals("pending", ignoreCase = true) }

            if (hasPendingSentInvite) {
                AuthenticatedEntryDestination.OUTGOING_INVITE
            } else {
                AuthenticatedEntryDestination.SPACE_SETUP
            }
        }
    }
}

class LoadSpaceSetupUseCase(
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
    private val getDefaultRelationshipSpace: GetDefaultRelationshipSpaceUseCase,
    private val getCurrentUserProfile: GetCurrentUserProfileUseCase,
    private val getInboxInvites: GetInboxInvitesUseCase,
    private val getSentInvites: GetSentInvitesUseCase,
) {
    suspend operator fun invoke(): SpaceSetupSnapshotModel {
        runCatching { syncRelationshipSpaces() }
        val profile = requireNotNull(getCurrentUserProfile()) { "No active profile found." }
        val inboxInvites = runCatching { getInboxInvites() }.getOrDefault(emptyList())
        val sentInvites = runCatching { getSentInvites() }.getOrDefault(emptyList())
        return SpaceSetupSnapshotModel(
            ownIdentifier = profile.identifier.uppercase(),
            displayName = profile.displayName,
            hasRelationshipSpace = getDefaultRelationshipSpace() != null,
            pendingIncomingInvites = inboxInvites.count { it.status.equals("pending", ignoreCase = true) },
            pendingSentInvites = sentInvites.count { it.status.equals("pending", ignoreCase = true) },
        )
    }
}

class SubmitPartnerInviteCodeUseCase(
    private val submitRelationshipInvite: RelationshipInviteSubmissionUseCase,
) {
    suspend operator fun invoke(identifier: String): RelationshipInviteSubmissionResult {
        return submitRelationshipInvite(identifier.trim().uppercase(), null)
    }
}
