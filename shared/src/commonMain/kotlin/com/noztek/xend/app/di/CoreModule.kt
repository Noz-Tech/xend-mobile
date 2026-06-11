package com.noztek.xend.app.di

import com.noztek.xend.app.AppConfig
import com.noztek.xend.app.StartupViewModel
import com.noztek.xend.core.network.ApiHealthChecker
import com.noztek.xend.core.network.createHttpClient
import com.noztek.xend.feature.device.domain.usecase.EnsureLocalSignalBootstrapUseCase
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun coreModule(appConfig: AppConfig) = module {
    single { appConfig }
    single(named("api_base_url")) { get<AppConfig>().apiBaseUrl }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    single<HttpClient> { createHttpClient(get()) }
    single<Settings> { Settings() }
    single { ApiHealthChecker(client = get(), baseUrl = get(named("api_base_url"))) }
    single { EnsureLocalSignalBootstrapUseCase(deviceDao = get(), signalBootstrapProvider = get()) }
    single {
        StartupViewModel(
            healthChecker = get(),
            ensureLocalSignalBootstrap = get(),
            getCurrentSession = get(),
            getPendingAuthFlow = get(),
            resolveAuthenticatedEntryDestination = get(),
        )
    }
}
