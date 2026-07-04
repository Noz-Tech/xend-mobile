package com.noztek.xend.app.di

import com.noztek.xend.feature.outgoinginvite.domain.usecase.LoadOutgoingInviteUseCase
import com.noztek.xend.feature.outgoinginvite.presentation.viewmodel.OutgoingInviteViewModel
import org.koin.dsl.module

val outgoingInviteModule = module {
    factory {
        LoadOutgoingInviteUseCase(
            syncRelationshipSpaces = get(),
            getDefaultRelationshipSpace = get(),
            getSentInvites = get(),
        )
    }
    factory {
        OutgoingInviteViewModel(
            loadOutgoingInvite = get(),
            declineRelationshipInvite = get(),
            realtimeSignals = get(),
        )
    }
}
