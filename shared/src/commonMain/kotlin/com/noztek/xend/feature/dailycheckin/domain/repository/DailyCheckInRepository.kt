package com.noztek.xend.feature.dailycheckin.domain.repository

import com.noztek.xend.feature.dailycheckin.domain.model.DailyCheckInStatusModel

interface DailyCheckInRepository {
    suspend fun getStatus(spaceId: String): DailyCheckInStatusModel
    suspend fun submitTodayCheckIn(spaceId: String): DailyCheckInStatusModel
}
