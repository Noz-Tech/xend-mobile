package com.noztek.xend.app.di

import com.noztek.xend.feature.dailyritual.data.impl.MockDailyRitualRepository
import com.noztek.xend.feature.dailyritual.domain.repository.DailyRitualRepository
import com.noztek.xend.feature.dailyritual.domain.usecase.GetDailyRitualOverviewUseCase
import org.koin.dsl.module

val dailyRitualModule = module {
    single<DailyRitualRepository> { MockDailyRitualRepository() }
    factory { GetDailyRitualOverviewUseCase(get()) }
}
