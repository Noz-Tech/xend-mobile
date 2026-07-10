package com.noztek.xend.app.di

import com.noztek.xend.feature.auth.data.impl.AuthRepositoryImpl
import com.noztek.xend.feature.auth.data.impl.PendingAuthFlowRepositoryImpl
import com.noztek.xend.feature.auth.data.remote.AuthApi
import com.noztek.xend.feature.auth.domain.repository.AuthRepository
import com.noztek.xend.feature.auth.domain.repository.PendingAuthFlowRepository
import com.noztek.xend.feature.auth.domain.usecase.ClearPendingAuthFlowUseCase
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.GetPendingAuthFlowUseCase
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.noztek.xend.feature.auth.domain.usecase.LoginUseCase
import com.noztek.xend.feature.auth.domain.usecase.LogoutUseCase
import com.noztek.xend.feature.auth.domain.usecase.RefreshSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.CompleteLoginSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.CompleteLogoutSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.RegisterWithEmailUseCase
import com.noztek.xend.feature.auth.domain.usecase.RegisterUserUseCase
import com.noztek.xend.feature.auth.domain.usecase.ResendVerificationCodeUseCase
import com.noztek.xend.feature.auth.domain.usecase.ResendVerificationUseCase
import com.noztek.xend.feature.auth.domain.usecase.SavePendingEmailVerificationUseCase
import com.noztek.xend.feature.auth.domain.usecase.VerifyEmailCodeUseCase
import com.noztek.xend.feature.auth.domain.usecase.VerifyEmailUseCase
import com.noztek.xend.feature.auth.domain.usecase.HasAnyLocalUserProfileUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authModule = module {
    single { AuthApi(client = get(), baseUrl = get(named("api_base_url"))) }

    single<AuthRepository> {
        AuthRepositoryImpl(
            api = get(),
            authSessionDao = get(),
            deviceDao = get(),
            userDao = get(),
        )
    }
    single<PendingAuthFlowRepository> { PendingAuthFlowRepositoryImpl(settings = get()) }

    factory { RegisterUserUseCase(get()) }
    factory { RegisterWithEmailUseCase(deviceDao = get(), registerUser = get()) }
    factory { VerifyEmailUseCase(get()) }
    factory { VerifyEmailCodeUseCase(get()) }
    factory { ResendVerificationUseCase(get()) }
    factory { ResendVerificationCodeUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { CompleteLoginSessionUseCase(deviceKeysSyncer = get(), realtimeSessionCoordinator = get()) }
    factory { CompleteLogoutSessionUseCase(realtimeSessionCoordinator = get()) }
    factory { RefreshSessionUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetCurrentSessionUseCase(get()) }
    factory { GetCurrentUserProfileUseCase(get()) }
    factory { HasAnyLocalUserProfileUseCase(get()) }
    factory { GetPendingAuthFlowUseCase(get()) }
    factory { SavePendingEmailVerificationUseCase(get()) }
    factory { ClearPendingAuthFlowUseCase(get()) }
}
