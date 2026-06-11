package com.noztek.xend.feature.auth.data.impl

import com.noztek.xend.feature.auth.domain.model.PendingAuthFlowModel
import com.noztek.xend.feature.auth.domain.model.PendingAuthFlowStep
import com.noztek.xend.feature.auth.domain.repository.PendingAuthFlowRepository
import com.russhwolf.settings.Settings

class PendingAuthFlowRepositoryImpl(
    private val settings: Settings,
) : PendingAuthFlowRepository {
    override suspend fun getPendingAuthFlow(): PendingAuthFlowModel? {
        val stepName = settings.getStringOrNull(KEY_STEP) ?: return null
        val email = settings.getStringOrNull(KEY_EMAIL)?.trim().orEmpty()
        if (email.isBlank()) {
            clearPendingAuthFlow()
            return null
        }
        val step = PendingAuthFlowStep.entries.firstOrNull { it.name == stepName }
        if (step == null) {
            clearPendingAuthFlow()
            return null
        }
        return PendingAuthFlowModel(
            step = step,
            email = email,
            createdAtEpochSeconds = settings.getLong(KEY_CREATED_AT, 0L),
        )
    }

    override suspend fun savePendingAuthFlow(flow: PendingAuthFlowModel) {
        settings.putString(KEY_STEP, flow.step.name)
        settings.putString(KEY_EMAIL, flow.email.trim().lowercase())
        settings.putLong(KEY_CREATED_AT, flow.createdAtEpochSeconds)
    }

    override suspend fun clearPendingAuthFlow() {
        settings.remove(KEY_STEP)
        settings.remove(KEY_EMAIL)
        settings.remove(KEY_CREATED_AT)
    }

    private companion object {
        const val KEY_STEP = "auth.pending_flow.step"
        const val KEY_EMAIL = "auth.pending_flow.email"
        const val KEY_CREATED_AT = "auth.pending_flow.created_at"
    }
}
