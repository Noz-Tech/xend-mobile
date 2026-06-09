package com.noztek.xend.feature.message.data.local.dao

import org.noztek.Database

data class ConversationLocal(
    val conversationId: String,
    val relationshipSpaceId: String,
    val createdByUserId: String,
    val archivedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)

class ConversationDao(
    private val db: Database,
) {
    fun upsertConversation(
        conversationId: String,
        relationshipSpaceId: String,
        createdByUserId: String,
        archivedAt: Long?,
        createdAt: Long,
        updatedAt: Long,
    ) {
        db.conversationsQueries.upsertConversation(
            conversation_id = conversationId,
            relationship_space_id = relationshipSpaceId,
            created_by_user_id = createdByUserId,
            archived_at = archivedAt,
            created_at = createdAt,
            updated_at = updatedAt,
        )
    }

    fun getById(conversationId: String): ConversationLocal? =
        db.conversationsQueries.selectConversationById(conversationId).executeAsOneOrNull()?.let {
            ConversationLocal(
                conversationId = it.conversation_id,
                relationshipSpaceId = it.relationship_space_id,
                createdByUserId = it.created_by_user_id,
                archivedAt = it.archived_at,
                createdAt = it.created_at,
                updatedAt = it.updated_at,
            )
        }

    fun getBySpaceId(relationshipSpaceId: String): ConversationLocal? =
        db.conversationsQueries.selectConversationBySpaceId(relationshipSpaceId).executeAsOneOrNull()?.let {
            ConversationLocal(
                conversationId = it.conversation_id,
                relationshipSpaceId = it.relationship_space_id,
                createdByUserId = it.created_by_user_id,
                archivedAt = it.archived_at,
                createdAt = it.created_at,
                updatedAt = it.updated_at,
            )
        }
}
