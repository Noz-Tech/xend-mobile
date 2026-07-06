package com.noztek.xend.feature.dailycheckin.data.impl

import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.feature.dailycheckin.data.remote.DailyCheckInApi
import com.noztek.xend.feature.dailycheckin.data.remote.DailyCheckInStatusDto
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInMilestoneAwardModel
import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInStatusModel
import com.noztek.xend.feature.dailycheckin.domain.repository.DailyCheckInRepository

class DailyCheckInRepositoryImpl(
    private val authSessionDao: AuthSessionDao,
    private val api: DailyCheckInApi,
) : DailyCheckInRepository {
    override suspend fun getStatus(spaceId: String): DailyCheckInStatusModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        return api.getTodayStatus(session.accessToken, spaceId).toModel()
    }

    override suspend fun submitTodayCheckIn(spaceId: String): DailyCheckInStatusModel {
        val session = requireNotNull(authSessionDao.getCurrentSession()) { "No active session" }
        return api.submitTodayCheckIn(session.accessToken, spaceId).toModel()
    }

    private fun DailyCheckInStatusDto.toModel(): DailyCheckInStatusModel {
        return DailyCheckInStatusModel(
            relationshipSpaceId = relationshipSpaceId,
            checkInDate = checkInDate,
            myCheckedIn = myCheckedIn,
            partnerCheckedIn = partnerCheckedIn,
            allMembersCheckedIn = allMembersCheckedIn,
            completedDaysCount = completedDaysCount,
            currentStreak = currentStreak,
            dailyRewardAwarded = dailyRewardAwarded,
            dailyRewardPoints = dailyRewardPoints,
            milestoneAward = milestoneAward?.let {
                DailyCheckInMilestoneAwardModel(
                    milestoneId = it.milestoneId,
                    completedDays = it.completedDays,
                    bonusPoints = it.bonusPoints,
                    title = it.title,
                    description = it.description,
                )
            },
            totalCheckInBondPointsEarned = totalCheckInBondPointsEarned,
        )
    }
}
