package com.noztek.xend.app.di

import com.noztek.xend.app.AppConfig

fun appModules(appConfig: AppConfig) = listOf(
    coreModule(appConfig),
    databaseModule,
    welcomeModule,
    authModule,
    deviceModule,
    inviteModule,
    incomingInviteModule,
    outgoingInviteModule,
    dailyCheckInModule,
    dailyRitualModule,
    gamesModule,
    challengesModule,
    spaceSetupModule,
    spaceModule,
    messageModule,
    presentationModule,
)
