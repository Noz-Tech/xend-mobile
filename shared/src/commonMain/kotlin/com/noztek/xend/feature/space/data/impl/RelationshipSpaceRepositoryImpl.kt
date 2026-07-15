package com.noztek.xend.feature.space.data.impl

import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.feature.space.data.local.dao.RelationshipSpaceCardLocal
import com.noztek.xend.feature.space.data.local.dao.RelationshipSpaceDao
import com.noztek.xend.feature.space.data.remote.SpaceApi
import com.noztek.xend.feature.space.data.remote.SpaceMoodDto
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.model.SpaceMoodModel
import com.noztek.xend.feature.space.domain.repository.RelationshipSpaceRepository

class RelationshipSpaceRepositoryImpl(
    private val authSessionDao: AuthSessionDao,
    private val dao: RelationshipSpaceDao,
    private val api: SpaceApi,
) : RelationshipSpaceRepository {
    override suspend fun getDefaultSpace(): RelationshipSpaceCardModel? = dao.getDefaultSpaceCard()?.let(::toModel)

    override suspend fun getHiddenSpaces(): List<RelationshipSpaceCardModel> = dao.getHiddenSpaceCards().map(::toModel)

    override suspend fun getCurrentMoods(spaceId: String): List<SpaceMoodModel> {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        return api.getCurrentMoods(session.accessToken, spaceId).map(::toMoodModel)
    }

    override suspend fun setMood(spaceId: String, moodKey: String, emoji: String, label: String): List<SpaceMoodModel> {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        return api.setMood(session.accessToken, spaceId, moodKey, emoji, label).map(::toMoodModel)
    }

    override suspend fun setDefaultSpace(spaceId: String) {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        api.setDefaultSpace(session.accessToken, spaceId)
    }

    override suspend fun configureSpaceAccess(spaceId: String, passphrase: String, hint: String?) {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        api.configureSpaceAccess(session.accessToken, spaceId, passphrase, hint)
    }

    override suspend fun unlockSpace(passphrase: String): RelationshipSpaceCardModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        val space = api.unlockSpace(session.accessToken, passphrase)
        dao.upsertSpace(
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
        return RelationshipSpaceCardModel(
            relationshipSpaceId = space.relationshipSpaceId,
            conversationId = space.conversationId,
            name = space.name ?: "Unnamed Space",
            currentLevel = space.currentLevel,
            currentLevelName = space.currentLevelName,
            currentPoints = 0,
            requiredPoints = 100,
            isDefault = space.isDefault,
            accessHint = space.accessHint,
            accessConfigured = space.accessConfigured,
            createdAtEpochSeconds = space.createdAt,
            updatedAtEpochSeconds = space.updatedAt,
        )
    }

    private fun toModel(local: RelationshipSpaceCardLocal): RelationshipSpaceCardModel {
        return RelationshipSpaceCardModel(
            relationshipSpaceId = local.relationshipSpaceId,
            conversationId = local.conversationId,
            name = local.name,
            currentLevel = local.currentLevel,
            currentLevelName = local.currentLevelName,
            currentPoints = local.currentPoints,
            requiredPoints = local.requiredPoints,
            isDefault = local.isDefault,
            accessHint = local.accessHint,
            accessConfigured = local.accessConfigured,
            createdAtEpochSeconds = local.createdAtEpochSeconds,
            updatedAtEpochSeconds = local.updatedAtEpochSeconds,
        )
    }

    private fun toMoodModel(dto: SpaceMoodDto): SpaceMoodModel {
        return SpaceMoodModel(
            relationshipSpaceId = dto.relationshipSpaceId,
            userId = dto.userId,
            displayName = dto.displayName,
            moodKey = dto.moodKey,
            emoji = dto.emoji,
            label = dto.label,
            updatedAtEpochSeconds = dto.updatedAt,
            isMe = dto.isMe,
        )
    }
}
