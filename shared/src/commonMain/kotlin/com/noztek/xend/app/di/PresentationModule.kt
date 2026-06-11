package com.noztek.xend.app.di

import com.noztek.xend.core.realtime.NoopPresenceGateway
import com.noztek.xend.core.realtime.NoopRealtimeFeatureSignals
import com.noztek.xend.core.realtime.NoopRealtimeSessionCoordinator
import com.noztek.xend.core.realtime.PresenceGateway
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.core.realtime.RealtimeSessionCoordinator
import com.noztek.xend.feature.invites.domain.usecase.DefaultRelationshipInviteAcceptanceCompleter
import com.noztek.xend.feature.invites.domain.usecase.RelationshipInviteSubmissionUseCase
import com.noztek.xend.feature.invites.domain.usecase.RelationshipInviteAcceptanceCompleter
import com.noztek.xend.feature.invites.presentation.viewmodel.InviteBadgeViewModel
import com.noztek.xend.feature.invites.presentation.viewmodel.InvitePartnerViewModel
import com.noztek.xend.feature.invites.presentation.viewmodel.InvitesViewModel
import com.noztek.xend.feature.auth.presentation.viewmodel.AuthViewModel
import com.noztek.xend.feature.space.presentation.viewmodel.HiddenSpacesViewModel
import com.noztek.xend.feature.space.presentation.viewmodel.SpaceDetailsViewModel
import com.noztek.xend.feature.space.presentation.viewmodel.SpaceViewModel
import org.koin.dsl.module

val presentationModule = module {
    single<PresenceGateway> { NoopPresenceGateway() }
    single<RealtimeFeatureSignals> { NoopRealtimeFeatureSignals() }
    single<RealtimeSessionCoordinator> { NoopRealtimeSessionCoordinator() }
    single<RelationshipInviteAcceptanceCompleter> {
        DefaultRelationshipInviteAcceptanceCompleter(
            acceptRelationshipInvite = get(),
            syncRelationshipSpaces = get(),
        )
    }
    factory { RelationshipInviteSubmissionUseCase(createRelationshipInvite = get(), getSentInvites = get()) }

    factory { InvitePartnerViewModel(submitRelationshipInvite = get(), getSentInvites = get()) }
    factory { InviteBadgeViewModel(getInboxInvites = get(), realtimeSignals = get()) }
    factory {
        AuthViewModel(
            registerWithEmailAction = get(),
            verifyEmailCodeAction = get(),
            resendVerificationCodeAction = get(),
            savePendingEmailVerification = get(),
            clearPendingAuthFlow = get(),
            login = get(),
            refreshSession = get(),
            completeLoginSession = get(),
            logout = get(),
            completeLogoutSession = get(),
            getCurrentSession = get(),
            getCurrentProfile = get(),
        )
    }
    factory {
        InvitesViewModel(
            getInboxInvites = get(),
            getSentInvites = get(),
            completeInviteAcceptance = get(),
            declineRelationshipInvite = get(),
            realtimeSignals = get(),
        )
    }
    factory {
        SpaceViewModel(
            getUnreadCount = get(),
            getDefaultRelationshipSpace = get(),
            syncRelationshipSpaces = get(),
            realtimeSignals = get(),
        )
    }
    factory { SpaceDetailsViewModel(getRelationshipSpaceById = get()) }
    factory {
        HiddenSpacesViewModel(
            getHiddenRelationshipSpaces = get(),
            setDefaultRelationshipSpace = get(),
            configureRelationshipSpaceAccess = get(),
            unlockRelationshipSpace = get(),
            syncRelationshipSpaces = get(),
        )
    }
}
