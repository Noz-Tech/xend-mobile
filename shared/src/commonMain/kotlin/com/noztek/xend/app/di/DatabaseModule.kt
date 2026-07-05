package com.noztek.xend.app.di

import app.cash.sqldelight.db.SqlDriver
import com.noztek.xend.core.database.DatabaseDriverFactory
import com.noztek.xend.feature.auth.data.local.dao.AuthSessionDao
import com.noztek.xend.feature.auth.data.local.dao.UserDao
import com.noztek.xend.feature.device.data.local.dao.DeviceDao
import com.noztek.xend.feature.device.data.local.dao.KyberPrekeyDao
import com.noztek.xend.feature.device.data.local.dao.OneTimePrekeyDao
import com.noztek.xend.feature.device.data.local.dao.SignalSessionDao
import com.noztek.xend.feature.device.data.local.dao.SignedPrekeyDao
import com.noztek.xend.feature.message.data.local.dao.ConversationDao
import com.noztek.xend.feature.message.data.local.dao.MessageDao
import com.noztek.xend.feature.message.data.local.dao.MessageReactionDao
import com.noztek.xend.feature.message.data.local.dao.MessageReceiptDao
import com.noztek.xend.feature.space.data.local.dao.RelationshipSpaceMemberDao
import org.koin.dsl.module
import org.noztek.Database

val databaseModule = module {
    single<SqlDriver> { get<DatabaseDriverFactory>().createDriver() }
    single { Database(get()) }
    single { AuthSessionDao(get()) }
    single { UserDao(get()) }
    single { DeviceDao(get()) }
    single { ConversationDao(get()) }
    single { MessageDao(get()) }
    single { MessageReceiptDao(get()) }
    single { MessageReactionDao(get()) }
    single { RelationshipSpaceMemberDao(get()) }
    single { SignedPrekeyDao(get()) }
    single { OneTimePrekeyDao(get()) }
    single { KyberPrekeyDao(get()) }
    single { SignalSessionDao(get()) }
}
