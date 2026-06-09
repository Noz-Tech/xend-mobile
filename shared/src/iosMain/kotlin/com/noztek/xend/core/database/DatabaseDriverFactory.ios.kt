package com.noztek.xend.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.noztek.xend.core.security.DatabasePassphraseProvider
import co.touchlab.sqliter.DatabaseConfiguration
import org.noztek.Database

private const val DB_NAME = "xend.db"

actual class DatabaseDriverFactory(
    private val passphraseProvider: DatabasePassphraseProvider,
) {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = Database.Schema,
        name = DB_NAME,
        onConfiguration = { configuration ->
            configuration.copy(
                encryptionConfig = DatabaseConfiguration.Encryption(
                    key = passphraseProvider.getPassphrase(),
                ),
            )
        },
    )
}
