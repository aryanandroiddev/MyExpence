package dev.app.arya.myexpence.data

import android.content.Context
import androidx.work.WorkManager
import dev.app.arya.myexpence.data.db.AppDatabase
import dev.app.arya.myexpence.data.ingest.CsvImporter
import dev.app.arya.myexpence.data.ingest.SmsImporter
import dev.app.arya.myexpence.data.repo.BudgetRepository
import dev.app.arya.myexpence.data.repo.CategoryRepository
import dev.app.arya.myexpence.data.repo.AnalyticsRepository
import dev.app.arya.myexpence.data.repo.SettingsRepository
import dev.app.arya.myexpence.data.repo.TransactionRepository

interface AppContainer {
    val db: AppDatabase
    val workManager: WorkManager

    val settingsRepository: SettingsRepository
    val transactionRepository: TransactionRepository
    val categoryRepository: CategoryRepository
    val budgetRepository: BudgetRepository
    val analyticsRepository: AnalyticsRepository

    val smsImporter: SmsImporter
    val csvImporter: CsvImporter
}

class AppContainerImpl(
    private val appContext: Context,
) : AppContainer {
    override val db: AppDatabase by lazy { AppDatabase.create(appContext) }
    override val workManager: WorkManager by lazy { WorkManager.getInstance(appContext) }

    override val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }
    override val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(db.transactionDao(), db.categoryDao())
    }
    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(db.categoryDao())
    }
    override val budgetRepository: BudgetRepository by lazy {
        BudgetRepository(db.budgetDao(), db.transactionDao())
    }
    override val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepository(db.transactionDao(), db.categoryDao())
    }

    override val smsImporter: SmsImporter by lazy {
        SmsImporter(appContext, db.ingestionStateDao(), db.transactionDao())
    }
    override val csvImporter: CsvImporter by lazy {
        CsvImporter(db.ingestionStateDao(), db.transactionDao())
    }
}

