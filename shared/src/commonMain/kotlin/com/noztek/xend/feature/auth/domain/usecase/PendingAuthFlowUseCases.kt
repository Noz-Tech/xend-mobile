package com.noztek.xend.feature.auth.domain.usecase

import com.noztek.xend.core.time.currentEpochSeconds
import com.noztek.xend.feature.auth.domain.model.PendingAuthFlowModel
import com.noztek.xend.feature.auth.domain.model.PendingAuthFlowStep
import com.noztek.xend.feature.auth.domain.repository.PendingAuthFlowRepository

class GetPendingAuthFlowUseCase(
    private val repository: PendingAuthFlowRepository,
) {
    suspend operator fun invoke(): PendingAuthFlowModel? = repository.getPendingAuthFlow()
}

class SavePendingEmailVerificationUseCase(
    private val repository: PendingAuthFlowRepository,
) {
    suspend operator fun invoke(email: String) {
        repository.savePendingAuthFlow(
            PendingAuthFlowModel(
                step = PendingAuthFlowStep.VERIFY_EMAIL,
                email = email.trim().lowercase(),
                createdAtEpochSeconds = currentEpochSeconds(),
            ),
        )
    }
}

class ClearPendingAuthFlowUseCase(
    private val repository: PendingAuthFlowRepository,
) {
    suspend operator fun invoke() = repository.clearPendingAuthFlow()
}
