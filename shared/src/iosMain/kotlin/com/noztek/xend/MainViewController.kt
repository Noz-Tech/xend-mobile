package com.noztek.xend

import com.noztek.xend.app.AppConfig
import com.noztek.xend.app.di.initKoinIos
import com.noztek.xend.core.crypto.SignalBootstrapProvider
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(
    appConfig: AppConfig,
    signalBootstrapProvider: SignalBootstrapProvider,
) = ComposeUIViewController {
    initKoinIos(appConfig, signalBootstrapProvider)
    App()
}
