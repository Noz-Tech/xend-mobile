package com.noztek.xend.feature.space.domain.repository

import androidx.compose.ui.graphics.ImageBitmap
import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.model.SpaceMoodModel

interface RelationshipSpaceRepository {
    suspend fun getDefaultSpace(): RelationshipSpaceCardModel?
    suspend fun getHiddenSpaces(): List<RelationshipSpaceCardModel>
    suspend fun getCurrentMoods(spaceId: String): List<SpaceMoodModel>
    suspend fun setMood(spaceId: String, moodKey: String, emoji: String, label: String): List<SpaceMoodModel>
    suspend fun setDefaultSpace(spaceId: String)
    suspend fun updateSpaceSettings(spaceId: String, name: String?, relationshipStartDate: String? = null): RelationshipSpaceCardModel
    suspend fun uploadCoverPhoto(spaceId: String, image: PickedImageData): RelationshipSpaceCardModel
    suspend fun uploadCouplePhoto(spaceId: String, image: PickedImageData): RelationshipSpaceCardModel
    suspend fun getSpaceMediaImage(spaceId: String, kind: String): ImageBitmap
    suspend fun configureSpaceAccess(spaceId: String, passphrase: String, hint: String?)
    suspend fun unlockSpace(passphrase: String): RelationshipSpaceCardModel
}
