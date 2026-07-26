package com.noztek.xend.feature.space.domain.model

data class RelationshipSpaceCardModel(
    val relationshipSpaceId: String,
    val conversationId: String,
    val name: String,
    val coverPhotoUrl: String?,
    val coverPhotoVersion: String?,
    val couplePhotoUrl: String?,
    val couplePhotoVersion: String?,
    val relationshipStartDate: String,
    val celebrateMonthsary: Boolean,
    val celebrateAnniversary: Boolean,
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
