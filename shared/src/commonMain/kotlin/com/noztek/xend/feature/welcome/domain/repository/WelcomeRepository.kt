package com.noztek.xend.feature.welcome.domain.repository

interface WelcomeRepository {
    fun hasCompletedOnboarding(): Boolean
    fun markOnboardingCompleted()
}
