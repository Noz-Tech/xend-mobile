package com.noztek.xend.app.di

import com.noztek.xend.feature.message.data.remote.MessageApi
import org.koin.core.qualifier.named
import org.koin.dsl.module

val messageModule = module {
    single { MessageApi(client = get(), baseUrl = get(named("api_base_url"))) }
}
