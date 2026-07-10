package com.noztek.xend.app.di

import com.noztek.xend.feature.dailyritual.data.impl.DailyRitualRepositoryImpl
import com.noztek.xend.feature.dailyritual.data.remote.DailyRitualApi
import com.noztek.xend.feature.dailyritual.domain.repository.DailyRitualRepository
import com.noztek.xend.feature.dailyritual.domain.usecase.GetDailyRitualOverviewUseCase
import com.noztek.xend.feature.dailyritual.domain.usecase.SubmitDailyRitualUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dailyRitualModule = module {
    single { DailyRitualApi(client = get(), baseUrl = get(named("api_base_url"))) }
    single<DailyRitualRepository> {
        DailyRitualRepositoryImpl(
            authSessionDao = get(),
            api = get(),
        )
    }
    factory {
        GetDailyRitualOverviewUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            syncRelationshipSpaces = get(),
        )
    }
    factory {
        SubmitDailyRitualUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            syncRelationshipSpaces = get(),
            getOverview = get(),
        )
    }
}
