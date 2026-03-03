package dev.app.arya.myexpence.data.repo

import dev.app.arya.myexpence.data.db.CategoryDao
import dev.app.arya.myexpence.data.db.TransactionDao
import dev.app.arya.myexpence.data.db.TransactionTypeDb
import dev.app.arya.myexpence.domain.Category
import dev.app.arya.myexpence.domain.CategorySpend
import dev.app.arya.myexpence.domain.DashboardSummary

class AnalyticsRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) {
    suspend fun dashboardForMonth(
        monthStartEpochMs: Long,
        monthEndEpochMs: Long,
        currency: String,
    ): DashboardSummary {
        val income = transactionDao.sumForPeriod(TransactionTypeDb.INCOME, monthStartEpochMs, monthEndEpochMs)
        val expense = transactionDao.sumForPeriod(TransactionTypeDb.EXPENSE, monthStartEpochMs, monthEndEpochMs)
        val catTotals = transactionDao.categoryTotalsForPeriod(TransactionTypeDb.EXPENSE, monthStartEpochMs, monthEndEpochMs)

        val categories = categoryDao.getAll().associateBy { it.id }
        val breakdown = catTotals.map { row ->
            val cat = row.categoryId?.let { categories[it] }?.let { Category(it.id, it.name, it.isSystem) }
            CategorySpend(category = cat, totalMinor = row.totalMinor)
        }

        return DashboardSummary(
            monthStartEpochMs = monthStartEpochMs,
            currency = currency,
            incomeTotalMinor = income,
            expenseTotalMinor = expense,
            netMinor = income - expense,
            expenseByCategory = breakdown,
        )
    }
}

