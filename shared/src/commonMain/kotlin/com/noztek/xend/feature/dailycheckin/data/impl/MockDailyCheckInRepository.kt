package com.noztek.xend.feature.dailycheckin.data.impl

import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInStatusModel
import com.noztek.xend.feature.dailycheckin.domain.repository.DailyCheckInRepository

class MockDailyCheckInRepository : DailyCheckInRepository {
    override suspend fun getStatus(spaceId: String): DailyCheckInStatusModel {
        return DailyCheckInStatusModel(
            relationshipSpaceId = spaceId,
            checkInDate = "2026-07-06",
            myCheckedIn = true,
            partnerCheckedIn = true,
            allMembersCheckedIn = true,
            completedDaysCount = 7,
            currentStreak = 7,
            dailyRewardAwarded = true,
            dailyRewardPoints = 5,
            milestoneAward = null,
            totalCheckInBondPointsEarned = 40,
        )
    }

    override suspend fun submitTodayCheckIn(spaceId: String): DailyCheckInStatusModel = getStatus(spaceId)
}
