package com.noztek.xend

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.noztek.xend.app.AppConfig
import com.noztek.xend.app.di.initKoinAndroid
import com.noztek.xend.core.realtime.AndroidRealtimeFeatureHost
import com.noztek.xend.core.realtime.RealtimeSessionCoordinator
import org.koin.core.context.GlobalContext

class XendApplication : Application(), DefaultLifecycleObserver {
    override fun onCreate() {
        super<Application>.onCreate()
        System.loadLibrary("sqlcipher")
        initKoinAndroid(
            context = applicationContext,
            appConfig = AppConfig(
                apiBaseUrl = AndroidRuntimeConfig.apiBaseUrl(),
            ),
        )
        realtimeFeatureHost().start()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        realtimeCoordinator().onAppForeground()
    }

    override fun onStop(owner: LifecycleOwner) {
        realtimeCoordinator().onAppBackground()
    }

    private fun realtimeCoordinator(): RealtimeSessionCoordinator =
        GlobalContext.get().get()

    private fun realtimeFeatureHost(): AndroidRealtimeFeatureHost =
        GlobalContext.get().get()
}
