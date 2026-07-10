package com.noztek.xend.feature.welcome.domain.usecase

import com.noztek.xend.feature.welcome.domain.repository.WelcomeRepository

class HasCompletedOnboardingUseCase(
    private val repository: WelcomeRepository,
) {
    operator fun invoke(): Boolean = repository.hasCompletedOnboarding()
}

class MarkOnboardingCompletedUseCase(
    private val repository: WelcomeRepository,
) {
    operator fun invoke() = repository.markOnboardingCompleted()
}
