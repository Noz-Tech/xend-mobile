package com.noztek.xend.feature.invites.domain.usecase

import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase

interface RelationshipInviteAcceptanceCompleter {
    suspend fun accept(inviteId: String)
}

class DefaultRelationshipInviteAcceptanceCompleter(
    private val acceptRelationshipInvite: AcceptRelationshipInviteUseCase,
    private val syncRelationshipSpaces: SyncRelationshipSpacesUseCase,
) : RelationshipInviteAcceptanceCompleter {
    override suspend fun accept(inviteId: String) {
        acceptRelationshipInvite(inviteId)
        syncRelationshipSpaces()
    }
}
