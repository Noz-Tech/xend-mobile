package com.noztek.xend.feature.welcome.data.impl

import com.noztek.xend.feature.welcome.domain.repository.WelcomeRepository
import com.russhwolf.settings.Settings

class WelcomeRepositoryImpl(
    private val settings: Settings,
) : WelcomeRepository {
    override fun hasCompletedOnboarding(): Boolean = settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun markOnboardingCompleted() {
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, true)
    }

    private companion object {
        const val KEY_ONBOARDING_COMPLETED = "welcome.onboarding_completed"
    }
}
