package com.noztek.xend.app.di

import com.noztek.xend.feature.space.data.impl.RelationshipSpaceRepositoryImpl
import com.noztek.xend.feature.space.data.local.dao.RelationshipSpaceDao
import com.noztek.xend.feature.space.data.remote.SpaceApi
import com.noztek.xend.feature.space.domain.repository.RelationshipSpaceRepository
import com.noztek.xend.feature.space.domain.usecase.ConfigureRelationshipSpaceAccessUseCase
import com.noztek.xend.feature.space.domain.usecase.GetCurrentSpaceMoodsUseCase
import com.noztek.xend.feature.space.domain.usecase.GetDefaultSpaceHeroUseCase
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.GetHiddenRelationshipSpacesUseCase
import com.noztek.xend.feature.space.domain.usecase.SetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.domain.usecase.SetSpaceMoodUseCase
import com.noztek.xend.feature.space.domain.usecase.SyncRelationshipSpacesUseCase
import com.noztek.xend.feature.space.domain.usecase.UnlockRelationshipSpaceUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val spaceModule = module {
    single { SpaceApi(client = get(), baseUrl = get(named("api_base_url"))) }
    single { RelationshipSpaceDao(get()) }

    single<RelationshipSpaceRepository> {
        RelationshipSpaceRepositoryImpl(
            authSessionDao = get(),
            dao = get(),
            api = get(),
        )
    }

    factory { GetDefaultRelationshipSpaceUseCase(get()) }
    factory { GetDefaultSpaceHeroUseCase(getCurrentUserProfile = get(), memberDao = get()) }
    factory { GetHiddenRelationshipSpacesUseCase(get()) }
    factory { GetCurrentSpaceMoodsUseCase(get()) }
    factory { SetSpaceMoodUseCase(get()) }
    factory { SetDefaultRelationshipSpaceUseCase(get()) }
    factory { ConfigureRelationshipSpaceAccessUseCase(get()) }
    factory { UnlockRelationshipSpaceUseCase(get()) }
    factory {
        SyncRelationshipSpacesUseCase(
            authSessionDao = get(),
            spaceApi = get(),
            spaceDao = get(),
            memberDao = get(),
            conversationDao = get(),
        )
    }
}
