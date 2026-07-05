package com.noztek.xend.app.di

import com.noztek.xend.feature.games.data.impl.MockGamesRepository
import com.noztek.xend.feature.games.domain.repository.GamesRepository
import com.noztek.xend.feature.games.domain.usecase.GetGamesOverviewUseCase
import org.koin.dsl.module

val gamesModule = module {
    single<GamesRepository> { MockGamesRepository() }
    factory { GetGamesOverviewUseCase(get()) }
}
