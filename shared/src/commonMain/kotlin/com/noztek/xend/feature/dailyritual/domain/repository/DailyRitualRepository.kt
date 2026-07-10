package com.noztek.xend.feature.dailyritual.domain.repository

import com.noztek.xend.core.ui.media.PickedImageData
import com.noztek.xend.feature.dailyritual.domain.model.DailyRitualStatusModel

interface DailyRitualRepository {
    suspend fun getOverview(spaceId: String): DailyRitualStatusModel
    suspend fun submit(spaceId: String, assignmentId: String, textResponse: String? = null)
    suspend fun submitImage(spaceId: String, assignmentId: String, image: PickedImageData)
}
