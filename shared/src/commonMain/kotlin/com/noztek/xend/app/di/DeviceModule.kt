package com.noztek.xend.app.di

import com.noztek.xend.feature.device.data.remote.DeviceApi
import org.koin.core.qualifier.named
import org.koin.dsl.module

val deviceModule = module {
    single { DeviceApi(client = get(), baseUrl = get(named("api_base_url"))) }
}
