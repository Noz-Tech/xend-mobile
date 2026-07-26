package com.noztek.xend.feature.space.domain.usecase

import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.noztek.xend.feature.message.data.local.dao.ConversationDao
import com.noztek.xend.feature.space.data.local.dao.RelationshipSpaceDao
import com.noztek.xend.feature.space.data.local.dao.RelationshipSpaceMemberDao
import com.noztek.xend.feature.space.data.local.dao.RelationshipSpaceMemberLocal
import com.noztek.xend.feature.space.data.remote.SpaceApi
import com.noztek.xend.feature.space.domain.model.RelationshipSpaceCardModel
import com.noztek.xend.feature.space.domain.model.SpaceHeroModel
import com.noztek.xend.feature.space.domain.model.SpaceMoodModel
import com.noztek.xend.feature.space.domain.repository.RelationshipSpaceRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Clock

class GetDefaultRelationshipSpaceUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(): RelationshipSpaceCardModel? = repository.getDefaultSpace()
}

class GetDefaultSpaceHeroUseCase(
    private val getCurrentUserProfile: GetCurrentUserProfileUseCase,
    private val memberDao: RelationshipSpaceMemberDao,
) {
    suspend operator fun invoke(defaultSpace: RelationshipSpaceCardModel?): SpaceHeroModel? {
        val space = defaultSpace ?: return null
        val currentProfile = getCurrentUserProfile()
        val members = memberDao.getMembers(space.relationshipSpaceId)

        val currentUserId = currentProfile?.userId
        val currentMember = currentUserId?.let { userId -> members.firstOrNull { it.userId == userId } }
        val partnerMember = currentUserId?.let { userId -> members.firstOrNull { it.userId != userId } }
            ?: members.firstOrNull()

        val userName = currentProfile?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: currentMember?.displayName?.takeIf { it.isNotBlank() }
            ?: "You"
        val partnerName = partnerMember?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: "Your partner"
        val startEpochSeconds = runCatching {
            LocalDate.parse(space.relationshipStartDate)
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .epochSeconds
        }.getOrDefault(space.createdAtEpochSeconds)
        val connectedDays = ((Clock.System.now().epochSeconds - startEpochSeconds)
            .coerceAtLeast(0L) / 86_400L).toInt() + 1

        return SpaceHeroModel(
            userName = userName,
            partnerName = partnerName,
            connectedDays = connectedDays,
        )
    }
}

class GetHiddenRelationshipSpacesUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(): List<RelationshipSpaceCardModel> = repository.getHiddenSpaces()
}

class GetCurrentSpaceMoodsUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(spaceId: String): List<SpaceMoodModel> = repository.getCurrentMoods(spaceId)
}

class SetSpaceMoodUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(spaceId: String, moodKey: String, emoji: String, label: String): List<SpaceMoodModel> {
        return repository.setMood(spaceId, moodKey, emoji, label)
    }
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

class UpdateRelationshipSpaceSettingsUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(
        spaceId: String,
        name: String?,
        relationshipStartDate: String? = null,
        celebrateMonthsary: Boolean? = null,
        celebrateAnniversary: Boolean? = null,
    ): RelationshipSpaceCardModel {
        return repository.updateSpaceSettings(
            spaceId = spaceId,
            name = name,
            relationshipStartDate = relationshipStartDate,
            celebrateMonthsary = celebrateMonthsary,
            celebrateAnniversary = celebrateAnniversary,
        )
    }
}

class UploadRelationshipSpaceCoverPhotoUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(spaceId: String, image: PickedImageData): RelationshipSpaceCardModel {
        return repository.uploadCoverPhoto(spaceId, image)
    }
}

class UploadRelationshipSpaceCouplePhotoUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(spaceId: String, image: PickedImageData): RelationshipSpaceCardModel {
        return repository.uploadCouplePhoto(spaceId, image)
    }
}

class GetRelationshipSpaceMediaImageUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(spaceId: String, kind: String) = repository.getSpaceMediaImage(spaceId, kind)
}

class UnlockRelationshipSpaceUseCase(
    private val repository: RelationshipSpaceRepository,
) {
    suspend operator fun invoke(passphrase: String): RelationshipSpaceCardModel = repository.unlockSpace(passphrase)
}

class SyncRelationshipSpacesUseCase(
    private val authSessionDao: com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao,
    private val spaceApi: SpaceApi,
    private val spaceDao: RelationshipSpaceDao,
    private val memberDao: RelationshipSpaceMemberDao,
    private val conversationDao: ConversationDao,
) {
    suspend operator fun invoke() {
        val session = authSessionDao.getCurrentSession() ?: return

        val spaces = spaceApi.getSpaces(session.accessToken)
        spaces.forEach { space ->
            spaceDao.upsertSpace(
                relationshipSpaceId = space.relationshipSpaceId,
                name = space.name,
                createdByUserId = space.createdByUserId,
                currentLevel = space.currentLevel,
                currentLevelName = space.currentLevelName,
                coverPhotoUrl = space.coverPhotoUrl,
                coverPhotoVersion = space.coverPhotoVersion,
                couplePhotoUrl = space.couplePhotoUrl,
                couplePhotoVersion = space.couplePhotoVersion,
                relationshipStartDate = space.relationshipStartDate,
                celebrateMonthsary = space.celebrateMonthsary,
                celebrateAnniversary = space.celebrateAnniversary,
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
            val members = spaceApi.getSpaceMembers(session.accessToken, space.relationshipSpaceId)
            memberDao.replaceMembers(
                relationshipSpaceId = space.relationshipSpaceId,
                members = members.map { member ->
                    RelationshipSpaceMemberLocal(
                        relationshipSpaceId = space.relationshipSpaceId,
                        userId = member.userId,
                        displayName = member.displayName,
                        identifier = member.identifier,
                    )
                },
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
