package com.noztek.xend.app.di

import com.noztek.xend.feature.challenges.data.impl.ChallengesRepositoryImpl
import com.noztek.xend.feature.challenges.data.remote.ChallengesApi
import com.noztek.xend.feature.challenges.domain.repository.ChallengesRepository
import com.noztek.xend.feature.challenges.domain.usecase.AcceptChallengeUseCase
import com.noztek.xend.feature.challenges.domain.usecase.CompleteChallengeUseCase
import com.noztek.xend.feature.challenges.domain.usecase.GetChallengeSubmissionImageUseCase
import com.noztek.xend.feature.challenges.domain.usecase.DeclineChallengeUseCase
import com.noztek.xend.feature.challenges.domain.usecase.GetChallengesOverviewUseCase
import com.noztek.xend.feature.challenges.domain.usecase.SendChallengeUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val challengesModule = module {
    single { ChallengesApi(client = get(), baseUrl = get(named("api_base_url"))) }
    single<ChallengesRepository> {
        ChallengesRepositoryImpl(
            authSessionDao = get(),
            api = get(),
        )
    }
    factory {
        GetChallengesOverviewUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            getDefaultSpaceHero = get(),
            syncRelationshipSpaces = get(),
        )
    }
    factory {
        SendChallengeUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            getDefaultSpaceHero = get(),
            syncRelationshipSpaces = get(),
        )
    }
    factory {
        AcceptChallengeUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            getDefaultSpaceHero = get(),
            syncRelationshipSpaces = get(),
        )
    }
    factory {
        DeclineChallengeUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            getDefaultSpaceHero = get(),
            syncRelationshipSpaces = get(),
        )
    }
    factory {
        CompleteChallengeUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            getDefaultSpaceHero = get(),
            syncRelationshipSpaces = get(),
        )
    }
    factory {
        GetChallengeSubmissionImageUseCase(
            repository = get(),
            getDefaultRelationshipSpace = get(),
            syncRelationshipSpaces = get(),
        )
    }
}
