package com.noztek.xend.app.di

import com.noztek.xend.feature.welcome.data.impl.WelcomeRepositoryImpl
import com.noztek.xend.feature.welcome.domain.repository.WelcomeRepository
import com.noztek.xend.feature.welcome.domain.usecase.HasCompletedOnboardingUseCase
import com.noztek.xend.feature.welcome.domain.usecase.MarkOnboardingCompletedUseCase
import org.koin.dsl.module

val welcomeModule = module {
    single<WelcomeRepository> { WelcomeRepositoryImpl(settings = get()) }
    factory { HasCompletedOnboardingUseCase(get()) }
    factory { MarkOnboardingCompletedUseCase(get()) }
}
