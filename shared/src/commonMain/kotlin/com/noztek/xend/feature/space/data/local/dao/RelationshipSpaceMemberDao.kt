package com.noztek.xend.feature.space.data.local.dao

import com.noztek.xend.core.time.currentEpochSeconds
import org.noztek.Database

data class RelationshipSpaceMemberLocal(
    val relationshipSpaceId: String,
    val userId: String,
    val displayName: String,
    val identifier: String,
)

class RelationshipSpaceMemberDao(
    private val db: Database,
) {
    fun replaceMembers(
        relationshipSpaceId: String,
        members: List<RelationshipSpaceMemberLocal>,
    ) {
        db.transaction {
            db.relationshipSpaceMembersQueries.deleteRelationshipSpaceMembersBySpaceId(relationshipSpaceId)
            members.forEach { member ->
                db.relationshipSpaceMembersQueries.upsertRelationshipSpaceMember(
                    relationship_space_id = member.relationshipSpaceId,
                    user_id = member.userId,
                    display_name = member.displayName,
                    identifier = member.identifier,
                    updated_at = currentEpochSeconds(),
                )
            }
        }
    }

    fun getMembers(relationshipSpaceId: String): List<RelationshipSpaceMemberLocal> {
        return db.relationshipSpaceMembersQueries
            .selectRelationshipSpaceMembersBySpaceId(relationshipSpaceId)
            .executeAsList()
            .map {
                RelationshipSpaceMemberLocal(
                    relationshipSpaceId = it.relationship_space_id,
                    userId = it.user_id,
                    displayName = it.display_name,
                    identifier = it.identifier,
                )
            }
    }
}
