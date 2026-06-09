package com.noztek.xend.app.di

import com.noztek.xend.app.AppConfig
import com.noztek.xend.core.crypto.SignalBootstrapProvider
import com.noztek.xend.core.database.DatabaseDriverFactory
import com.noztek.xend.core.security.DatabasePassphraseProvider
import com.noztek.xend.feature.device.domain.usecase.DeviceKeysSyncer
import org.koin.dsl.module

fun initKoinIos(
    appConfig: AppConfig,
    signalBootstrapProvider: SignalBootstrapProvider,
) {
    initKoin(appConfig) {
        modules(
            module {
                single { DatabasePassphraseProvider() }
                single { DatabaseDriverFactory(get()) }
                single<SignalBootstrapProvider> { signalBootstrapProvider }
                single<DeviceKeysSyncer> {
                    object : DeviceKeysSyncer {
                        override suspend fun sync(accessToken: String, deviceId: String) = Unit
                    }
                }
            },
        )
    }
}
