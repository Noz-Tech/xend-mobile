package com.noztek.xend.app.di

import com.noztek.xend.feature.spacesetup.domain.usecase.LoadSpaceSetupUseCase
import com.noztek.xend.feature.spacesetup.domain.usecase.ResolveAuthenticatedEntryDestinationUseCase
import com.noztek.xend.feature.spacesetup.domain.usecase.SubmitPartnerInviteCodeUseCase
import com.noztek.xend.feature.spacesetup.presentation.viewmodel.SpaceSetupViewModel
import org.koin.dsl.module

val spaceSetupModule = module {
    factory {
        ResolveAuthenticatedEntryDestinationUseCase(
            syncRelationshipSpaces = get(),
            getDefaultRelationshipSpace = get(),
        )
    }
    factory {
        LoadSpaceSetupUseCase(
            syncRelationshipSpaces = get(),
            getDefaultRelationshipSpace = get(),
            getCurrentUserProfile = get(),
            getInboxInvites = get(),
            getSentInvites = get(),
        )
    }
    factory { SubmitPartnerInviteCodeUseCase(submitRelationshipInvite = get()) }
    factory {
        SpaceSetupViewModel(
            loadSpaceSetup = get(),
            submitPartnerInviteCode = get(),
        )
    }
}
