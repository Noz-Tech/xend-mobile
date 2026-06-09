package com.noztek.xend.feature.auth.data.local.dao

import com.noztek.xend.core.time.currentEpochSeconds
import com.noztek.xend.feature.auth.domain.model.UserProfileModel
import org.noztek.Database

class UserDao(
    private val db: Database,
) {
    fun saveUserProfile(profile: UserProfileModel) {
        val now = currentEpochSeconds()

        db.userProfileQueries.upsertUserProfile(
            user_id = profile.userId,
            display_name = profile.displayName,
            email = profile.email,
            avatar_url = profile.avatarUrl,
            identifier = profile.identifier,
            email_verified_at = null,
            updated_at = now,
            last_synced_at = now,
        )
    }

    fun getUserProfile(userId: String): UserProfileModel? =
        db.userProfileQueries.selectUserProfile(userId).executeAsOneOrNull()?.let {
            UserProfileModel(
                userId = it.user_id,
                displayName = it.display_name,
                email = it.email,
                avatarUrl = it.avatar_url,
                identifier = it.identifier,
            )
        }
}
