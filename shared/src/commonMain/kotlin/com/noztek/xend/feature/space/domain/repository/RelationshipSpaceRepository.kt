package com.noztek.xend.feature.space.domain.repository

import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel

interface RelationshipSpaceRepository {
    suspend fun getSpaceCards(): List<RelationshipSpaceCardModel>
    suspend fun getDefaultSpace(): RelationshipSpaceCardModel?
    suspend fun getHiddenSpaces(): List<RelationshipSpaceCardModel>
    suspend fun getSpaceById(spaceId: String): RelationshipSpaceCardModel?
    suspend fun setDefaultSpace(spaceId: String)
    suspend fun configureSpaceAccess(spaceId: String, passphrase: String, hint: String?)
    suspend fun unlockSpace(passphrase: String): RelationshipSpaceCardModel
}
