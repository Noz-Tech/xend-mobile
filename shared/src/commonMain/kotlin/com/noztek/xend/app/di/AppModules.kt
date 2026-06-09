package com.noztek.xend.app.di

import com.noztek.xend.app.AppConfig

fun appModules(appConfig: AppConfig) = listOf(
    coreModule(appConfig),
    databaseModule,
    authModule,
    deviceModule,
    inviteModule,
    spaceModule,
    messageModule,
    presentationModule,
)
