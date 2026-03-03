package dev.app.arya.myexpence.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import dev.app.arya.myexpence.data.security.DbPassphrase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        IngestionStateEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun ingestionStateDao(): IngestionStateDao

    companion object {
        private const val DB_NAME = "myexpence.db"

        fun create(context: Context): AppDatabase {
            SQLiteDatabase.loadLibs(context)

            val passphraseBytes = DbPassphrase.getOrCreate(context)
            val factory = SupportFactory(passphraseBytes)

            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .openHelperFactory(factory)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed categories via repository on first run; keep DB callback minimal.
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

