package com.noztek.xend.feature.space.domain.repository

import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.model.SpaceMoodModel

interface RelationshipSpaceRepository {
    suspend fun getDefaultSpace(): RelationshipSpaceCardModel?
    suspend fun getHiddenSpaces(): List<RelationshipSpaceCardModel>
    suspend fun getCurrentMoods(spaceId: String): List<SpaceMoodModel>
    suspend fun setMood(spaceId: String, moodKey: String, emoji: String, label: String): List<SpaceMoodModel>
    suspend fun setDefaultSpace(spaceId: String)
    suspend fun configureSpaceAccess(spaceId: String, passphrase: String, hint: String?)
    suspend fun unlockSpace(passphrase: String): RelationshipSpaceCardModel
}
