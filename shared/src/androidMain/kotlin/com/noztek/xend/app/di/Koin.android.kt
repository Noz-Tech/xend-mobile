package com.noztek.xend.app.di

import android.content.Context
import com.noztek.xend.app.AppConfig
import com.noztek.xend.core.crypto.AndroidSignalBootstrapProvider
import com.noztek.xend.core.crypto.SignalBootstrapProvider
import com.noztek.xend.core.database.DatabaseDriverFactory
import com.noztek.xend.core.notify.FirebasePushTokenProvider
import com.noztek.xend.core.notify.PushTokenProvider
import com.noztek.xend.core.realtime.AndroidPresenceGateway
import com.noztek.xend.core.realtime.AndroidRealtimeFeatureHost
import com.noztek.xend.core.realtime.PresenceApi
import com.noztek.xend.core.realtime.RealtimeFeatureSignals
import com.noztek.xend.core.realtime.PresenceGateway
import com.noztek.xend.core.realtime.RealtimeEventBus
import com.noztek.xend.core.realtime.RealtimePresenceManager
import com.noztek.xend.core.realtime.RealtimeSessionCoordinator
import com.noztek.xend.feature.device.domain.usecase.BootstrapSignalSessionsOnAcceptUseCase
import com.noztek.xend.core.security.DatabasePassphraseProvider
import com.noztek.xend.feature.device.domain.usecase.DeviceKeysSyncer
import com.noztek.xend.feature.device.domain.usecase.SignalSessionBootstrapper
import com.noztek.xend.feature.device.domain.usecase.SyncDeviceKeysUseCase
import com.noztek.xend.feature.message.data.crypto.SecureMessageCipher
import com.noztek.xend.feature.message.data.crypto.SignalMessageCipher
import com.noztek.xend.feature.message.data.impl.MessageRepositoryImpl
import com.noztek.xend.feature.message.domain.repository.MessageRepository
import com.noztek.xend.feature.message.domain.usecase.GetConversationHeaderUseCase
import com.noztek.xend.feature.message.domain.usecase.GetConversationMessagesUseCase
import com.noztek.xend.feature.message.domain.usecase.GetCurrentChatSessionUseCase
import com.noztek.xend.feature.message.domain.usecase.LoadConversationEntryUseCase
import com.noztek.xend.feature.message.domain.usecase.GetUnreadCountUseCase
import com.noztek.xend.feature.message.domain.usecase.MarkConversationReadUseCase
import com.noztek.xend.feature.message.domain.usecase.RetryConversationMessageUseCase
import com.noztek.xend.feature.message.domain.usecase.RetryFailedMessageUseCase
import com.noztek.xend.feature.message.domain.usecase.SendConversationMessageUseCase
import com.noztek.xend.feature.message.domain.usecase.SendConversationTypingUseCase
import com.noztek.xend.feature.message.domain.usecase.SendSecureTextMessageUseCase
import com.noztek.xend.feature.message.domain.usecase.SyncMessagesUseCase
import com.noztek.xend.feature.message.domain.usecase.ToggleConversationReactionUseCase
import com.noztek.xend.feature.message.domain.usecase.ToggleMessageReactionUseCase
import com.noztek.xend.feature.message.presentation.viewmodel.MessageViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun initKoinAndroid(context: Context, appConfig: AppConfig) {
    initKoin(appConfig) {
        modules(
            module {
                single { context.applicationContext }
                single { DatabasePassphraseProvider(get()) }
                single { DatabaseDriverFactory(get(), get()) }
                single<SignalBootstrapProvider> { AndroidSignalBootstrapProvider() }
                single<PushTokenProvider> { FirebasePushTokenProvider() }
                single { RealtimeEventBus() }
                single { PresenceApi(client = get(), baseUrl = get(named("api_base_url"))) }
                single<PresenceGateway> { AndroidPresenceGateway(get()) }
                single {
                    AndroidRealtimeFeatureHost(
                        eventBus = get(),
                        syncRelationshipSpaces = get(),
                        bootstrapSignalSessions = get(),
                        syncMessages = get(),
                    )
                }
                single<RealtimeFeatureSignals> { get<AndroidRealtimeFeatureHost>() }
                single<RealtimeSessionCoordinator> {
                    RealtimePresenceManager(
                        client = get(),
                        authSessionDao = get(),
                        baseUrl = get(named("api_base_url")),
                        eventBus = get(),
                    )
                }
                factory {
                    SyncDeviceKeysUseCase(
                        deviceDao = get(),
                        signedPrekeyDao = get(),
                        kyberPrekeyDao = get(),
                        oneTimePrekeyDao = get(),
                        deviceApi = get(),
                        pushTokenProvider = get(),
                    )
                }
                single<DeviceKeysSyncer> {
                    object : DeviceKeysSyncer {
                        override suspend fun sync(accessToken: String, deviceId: String) {
                            get<SyncDeviceKeysUseCase>().invoke(accessToken, deviceId)
                        }

                        override suspend fun syncIfNeeded(accessToken: String, deviceId: String) {
                            val hasSignedPrekey = get<com.noztek.xend.feature.device.data.local.dao.SignedPrekeyDao>()
                                .getLatestForDevice(deviceId) != null
                            val hasKyberPrekey = get<com.noztek.xend.feature.device.data.local.dao.KyberPrekeyDao>()
                                .getLatestForDevice(deviceId) != null
                            val hasOneTimePrekeys = get<com.noztek.xend.feature.device.data.local.dao.OneTimePrekeyDao>()
                                .countAvailableForDevice(deviceId) > 0

                            if (hasSignedPrekey && hasKyberPrekey && hasOneTimePrekeys) return

                            get<SyncDeviceKeysUseCase>().invoke(accessToken, deviceId)
                        }
                    }
                }
                factory {
                    BootstrapSignalSessionsOnAcceptUseCase(
                        authSessionDao = get(),
                        deviceDao = get(),
                        spaceApi = get(),
                        deviceApi = get(),
                        signalSessionDao = get(),
                    )
                }
                single<SignalSessionBootstrapper> { get<BootstrapSignalSessionsOnAcceptUseCase>() }
                single {
                    SignalMessageCipher(
                        deviceDao = get(),
                        signedPrekeyDao = get(),
                        kyberPrekeyDao = get(),
                        oneTimePrekeyDao = get(),
                        signalSessionDao = get(),
                    )
                }
                single<SecureMessageCipher> { get<SignalMessageCipher>() }
                single<MessageRepository> {
                    MessageRepositoryImpl(
                        authSessionDao = get(),
                        conversationDao = get(),
                        messageDao = get(),
                        messageReceiptDao = get(),
                        messageReactionDao = get(),
                        messageApi = get(),
                        deviceApi = get(),
                        spaceApi = get(),
                        signalSessionDao = get(),
                        deviceKeysSyncer = get(),
                        signalMessageCipher = get(),
                        signalSessionBootstrapper = get(),
                    )
                }
                factory { SendSecureTextMessageUseCase(get()) }
                factory { RetryFailedMessageUseCase(get()) }
                factory { SyncMessagesUseCase(get()) }
                factory { MarkConversationReadUseCase(get()) }
                factory { GetUnreadCountUseCase(get()) }
                factory { GetConversationHeaderUseCase(get()) }
                factory { GetConversationMessagesUseCase(get()) }
                factory { GetCurrentChatSessionUseCase(get()) }
                factory { ToggleMessageReactionUseCase(get()) }
                factory {
                    LoadConversationEntryUseCase(
                        getConversationHeader = get(),
                        getConversationMessages = get(),
                        getCurrentChatSession = get(),
                        markConversationRead = get(),
                        presenceGateway = get(),
                        deviceKeysSyncer = get(),
                        syncMessages = get(),
                    )
                }
                factory {
                    SendConversationMessageUseCase(
                        getConversationMessages = get(),
                        sendSecureTextMessage = get(),
                        syncMessages = get(),
                    )
                }
                factory { SendConversationTypingUseCase(get()) }
                factory {
                    RetryConversationMessageUseCase(
                        getConversationMessages = get(),
                        retryFailedMessage = get(),
                        syncMessages = get(),
                    )
                }
                factory { ToggleConversationReactionUseCase(get()) }
                factory {
                    MessageViewModel(
                        loadConversationEntry = get(),
                        realtimeSignals = get(),
                        retryConversationMessage = get(),
                        sendConversationMessage = get(),
                        sendConversationTyping = get(),
                        toggleConversationReaction = get(),
                    )
                }
            },
        )
    }
}
