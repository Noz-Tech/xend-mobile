package com.noztek.xend.feature.auth.domain.repository

import com.noztek.xend.feature.auth.domain.model.PendingAuthFlowModel

interface PendingAuthFlowRepository {
    suspend fun getPendingAuthFlow(): PendingAuthFlowModel?
    suspend fun savePendingAuthFlow(flow: PendingAuthFlowModel)
    suspend fun clearPendingAuthFlow()
}
