package com.noztek.xend.feature.space.domain.usecase

import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.feature.message.data.local.dao.ConversationDao
import com.noztek.xend.feature.space.data.local.dao.RelationshipSpaceDao
import com.noztek.xend.feature.space.data.remote.SpaceApi
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.repository.RelationshipSpaceRepository

class GetRelationshipSpaceCardsUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(): List<RelationshipSpaceCardModel> = repository.getSpaceCards()
}

class GetDefaultRelationshipSpaceUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(): RelationshipSpaceCardModel? = repository.getDefaultSpace()
}

class GetHiddenRelationshipSpacesUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(): List<RelationshipSpaceCardModel> = repository.getHiddenSpaces()
}

class GetRelationshipSpaceByIdUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(spaceId: String): RelationshipSpaceCardModel? = repository.getSpaceById(spaceId)
}

class SetDefaultRelationshipSpaceUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(spaceId: String) {
        repository.setDefaultSpace(spaceId)
    }
}

class ConfigureRelationshipSpaceAccessUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(spaceId: String, passphrase: String, hint: String?) {
        repository.configureSpaceAccess(spaceId, passphrase, hint)
    }
}

class UnlockRelationshipSpaceUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(passphrase: String): RelationshipSpaceCardModel = repository.unlockSpace(passphrase)
}

class SyncRelationshipSpacesUseCase(
    private val authSessionDao: AuthSessionDao,
    private val spaceApi: SpaceApi,
    private val spaceDao: RelationshipSpaceDao,
    private val conversationDao: ConversationDao,
) {
    suspend operator fun invoke() {
        val session = authSessionDao.getCurrentSession() ?: return

        val levels = spaceApi.getLevels(session.accessToken)
        levels.forEach { level ->
            spaceDao.upsertLevel(
                level = level.level,
                name = level.name,
                description = level.description,
            )
        }

        val spaces = spaceApi.getSpaces(session.accessToken)
        spaces.forEach { space ->
            spaceDao.upsertSpace(
                relationshipSpaceId = space.relationshipSpaceId,
                name = space.name,
                createdByUserId = space.createdByUserId,
                currentLevel = space.currentLevel,
                currentLevelName = space.currentLevelName,
                isDefault = space.isDefault,
                accessHint = space.accessHint,
                accessConfigured = space.accessConfigured,
                archivedAt = space.archivedAt,
                createdAt = space.createdAt,
                updatedAt = space.updatedAt,
            )
            conversationDao.upsertConversation(
                conversationId = space.conversationId,
                relationshipSpaceId = space.relationshipSpaceId,
                createdByUserId = space.createdByUserId,
                archivedAt = space.archivedAt,
                createdAt = space.createdAt,
                updatedAt = space.updatedAt,
            )

            val progressRows = spaceApi.getLevelProgress(session.accessToken, space.relationshipSpaceId)
            progressRows.forEach { progress ->
                spaceDao.upsertLevelProgress(
                    relationshipSpaceId = progress.relationshipSpaceId,
                    level = progress.level,
                    requiredPoints = progress.requiredPoints,
                    currentPoints = progress.currentPoints,
                    unlockedAt = progress.unlockedAt,
                    createdAt = progress.createdAt,
                    updatedAt = progress.updatedAt,
                )
            }
        }
    }
}
