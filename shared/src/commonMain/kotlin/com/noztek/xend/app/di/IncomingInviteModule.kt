package com.noztek.xend.app.di

import com.noztek.xend.feature.incominginvite.domain.usecase.AcceptIncomingInviteUseCase
import com.noztek.xend.feature.incominginvite.domain.usecase.DeclineIncomingInviteUseCase
import com.noztek.xend.feature.incominginvite.domain.usecase.LoadIncomingInviteUseCase
import com.noztek.xend.feature.incominginvite.presentation.viewmodel.IncomingInviteViewModel
import org.koin.dsl.module

val incomingInviteModule = module {
    factory {
        LoadIncomingInviteUseCase(
            syncRelationshipSpaces = get(),
            getDefaultRelationshipSpace = get(),
            getInboxInvites = get(),
        )
    }
    factory { AcceptIncomingInviteUseCase(acceptanceCompleter = get()) }
    factory { DeclineIncomingInviteUseCase(declineRelationshipInvite = get()) }
    factory {
        IncomingInviteViewModel(
            loadIncomingInvite = get(),
            acceptIncomingInvite = get(),
            declineIncomingInvite = get(),
        )
    }
}
