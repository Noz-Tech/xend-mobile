package com.noztek.xend.app.di

import com.noztek.xend.feature.invites.data.impl.RelationshipInviteRepositoryImpl
import com.noztek.xend.feature.invites.data.remote.InviteApi
import com.noztek.xend.feature.invites.domain.repository.RelationshipInviteRepository
import com.noztek.xend.feature.invites.domain.usecase.AcceptRelationshipInviteUseCase
import com.noztek.xend.feature.invites.domain.usecase.CreateRelationshipInviteUseCase
import com.noztek.xend.feature.invites.domain.usecase.DeclineRelationshipInviteUseCase
import com.noztek.xend.feature.invites.domain.usecase.GetInboxInvitesUseCase
import com.noztek.xend.feature.invites.domain.usecase.GetSentInvitesUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val inviteModule = module {
    single { InviteApi(client = get(), baseUrl = get(named("api_base_url"))) }
    single { com.noztek.xend.feature.invites.data.local.dao.RelationshipInviteDao(get()) }

    single<RelationshipInviteRepository> {
        RelationshipInviteRepositoryImpl(
            authSessionDao = get(),
            inviteApi = get(),
            inviteDao = get(),
        )
    }

    factory { CreateRelationshipInviteUseCase(get()) }
    factory { AcceptRelationshipInviteUseCase(get()) }
    factory { DeclineRelationshipInviteUseCase(get()) }
    factory { GetInboxInvitesUseCase(get()) }
    factory { GetSentInvitesUseCase(get()) }
}
