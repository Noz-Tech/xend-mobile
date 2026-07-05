package com.noztek.xend.feature.space.data.local.dao

import org.noztek.Database

data class RelationshipSpaceCardLocal(
    val relationshipSpaceId: String,
    val conversationId: String,
    val name: String,
    val currentLevel: Int,
    val currentLevelName: String,
    val currentPoints: Int,
    val requiredPoints: Int,
    val isDefault: Boolean,
    val accessHint: String?,
    val accessConfigured: Boolean,
    val createdAtEpochSeconds: Long,
    val updatedAtEpochSeconds: Long,
)

class RelationshipSpaceDao(
    private val db: Database,
) {
    fun upsertSpace(
        relationshipSpaceId: String,
        name: String?,
        createdByUserId: String,
        currentLevel: Int,
        currentLevelName: String,
        isDefault: Boolean,
        accessHint: String?,
        accessConfigured: Boolean,
        archivedAt: Long?,
        createdAt: Long,
        updatedAt: Long,
    ) {
        db.relationshipSpacesQueries.upsertRelationshipSpace(
            relationship_space_id = relationshipSpaceId,
            name = name,
            created_by_user_id = createdByUserId,
            current_level = currentLevel.toLong(),
            current_level_name = currentLevelName,
            is_default = if (isDefault) 1 else 0,
            access_hint = accessHint,
            access_configured = if (accessConfigured) 1 else 0,
            archived_at = archivedAt,
            created_at = createdAt,
            updated_at = updatedAt,
        )
    }

    fun upsertLevelProgress(
        relationshipSpaceId: String,
        level: Int,
        requiredPoints: Int,
        currentPoints: Int,
        unlockedAt: Long?,
        createdAt: Long,
        updatedAt: Long,
    ) {
        db.relationshipLevelProgressQueries.upsertRelationshipLevelProgress(
            relationship_space_id = relationshipSpaceId,
            level = level.toLong(),
            required_points = requiredPoints.toLong(),
            current_points = currentPoints.toLong(),
            unlocked_at = unlockedAt,
            created_at = createdAt,
            updated_at = updatedAt,
        )
    }

    fun getDefaultSpaceCard(): RelationshipSpaceCardLocal? =
        db.relationshipSpacesQueries.selectDefaultRelationshipSpace().executeAsOneOrNull()?.let(::toCard)

    fun getHiddenSpaceCards(): List<RelationshipSpaceCardLocal> =
        db.relationshipSpacesQueries.selectHiddenRelationshipSpaces().executeAsList().map(::toCard)

    private fun toCard(space: org.noztek.RelationshipSpaces): RelationshipSpaceCardLocal {
        val progress = db.relationshipLevelProgressQueries
            .selectCurrentLevelProgress(space.relationship_space_id, space.current_level)
            .executeAsOneOrNull()

        return RelationshipSpaceCardLocal(
            relationshipSpaceId = space.relationship_space_id,
            conversationId = db.conversationsQueries
                .selectConversationBySpaceId(space.relationship_space_id)
                .executeAsOneOrNull()
                ?.conversation_id
                ?: "",
            name = space.name ?: "Unnamed Space",
            currentLevel = space.current_level.toInt(),
            currentLevelName = space.current_level_name,
            currentPoints = progress?.current_points?.toInt() ?: 0,
            requiredPoints = progress?.required_points?.toInt() ?: 100,
            isDefault = space.is_default == 1L,
            accessHint = space.access_hint,
            accessConfigured = space.access_configured == 1L,
            createdAtEpochSeconds = space.created_at,
            updatedAtEpochSeconds = space.updated_at,
        )
    }
}
