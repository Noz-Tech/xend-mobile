package com.noztek.xend.app.di

import com.noztek.xend.feature.challenges.data.impl.MockChallengesRepository
import com.noztek.xend.feature.challenges.domain.repository.ChallengesRepository
import com.noztek.xend.feature.challenges.domain.usecase.GetChallengesOverviewUseCase
import org.koin.dsl.module

val challengesModule = module {
    single<ChallengesRepository> { MockChallengesRepository() }
    factory { GetChallengesOverviewUseCase(get()) }
}
