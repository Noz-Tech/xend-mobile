package com.noztek.xend.app.di

import com.noztek.xend.app.AppConfig
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

fun initKoin(appConfig: AppConfig, configure: KoinApplication.() -> Unit = {}) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return

    startKoin {
        modules(appModules(appConfig))
        configure()
    }
}
