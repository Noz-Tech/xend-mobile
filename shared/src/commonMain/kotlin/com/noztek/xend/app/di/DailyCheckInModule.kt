package com.noztek.xend.app.di

import com.noztek.xend.feature.dailycheckin.data.impl.DailyCheckInRepositoryImpl
import com.noztek.xend.feature.dailycheckin.data.remote.DailyCheckInApi
import com.noztek.xend.feature.dailycheckin.domain.repository.DailyCheckInRepository
import com.noztek.xend.feature.dailycheckin.domain.usecase.GetDailyCheckInOverviewUseCase
import com.noztek.xend.feature.dailycheckin.domain.usecase.SubmitDailyCheckInUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dailyCheckInModule = module {
    single { DailyCheckInApi(client = get(), baseUrl = get(named("api_base_url"))) }
    single<DailyCheckInRepository> {
        DailyCheckInRepositoryImpl(
            authSessionDao = get(),
            api = get(),
        )
    }
    factory {
        GetDailyCheckInOverviewUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            getCurrentUserProfile = get(),
            memberDao = get(),
        )
    }
    factory {
        SubmitDailyCheckInUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            syncRelationshipSpaces = get(),
            getOverview = get(),
        )
    }
}
