package com.noztek.xend.core.database

import android.content.Context
import android.database.sqlite.SQLiteException
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.noztek.xend.core.security.DatabasePassphraseProvider
import net.zetetic.database.sqlcipher.SQLiteNotADatabaseException
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.noztek.Database

private const val DB_NAME = "xend.db"

actual class DatabaseDriverFactory(
    private val context: Context,
    private val passphraseProvider: DatabasePassphraseProvider,
) {
    actual fun createDriver(): SqlDriver {
        fun newDriver(): AndroidSqliteDriver {
            val passphrase = passphraseProvider.getPassphrase()
            return AndroidSqliteDriver(
                schema = Database.Schema,
                context = context,
                name = DB_NAME,
                factory = SupportOpenHelperFactory(passphrase),
            )
        }

        return try {
            newDriver().also(::validateDriver)
        } catch (_: SQLiteNotADatabaseException) {
            context.deleteDatabase(DB_NAME)
            newDriver().also(::validateDriver)
        }
    }
}

private fun validateDriver(driver: AndroidSqliteDriver) {
    driver.executeQuery(
        identifier = null,
        sql = "SELECT COUNT(*) FROM sqlite_schema;",
        mapper = { cursor ->
            cursor.next()
            QueryResult.Value(Unit)
        },
        parameters = 0,
    ).value
}
