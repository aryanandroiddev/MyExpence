package dev.app.arya.myexpence.data.repo

import dev.app.arya.myexpence.data.db.BudgetDao
import dev.app.arya.myexpence.data.db.BudgetEntity
import dev.app.arya.myexpence.data.db.TransactionDao
import dev.app.arya.myexpence.data.db.TransactionTypeDb
import dev.app.arya.myexpence.domain.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class BudgetRepository(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
) {
    fun observeAll(): Flow<List<BudgetEntity>> = budgetDao.observeAll()

    fun observeAllDomain(categoryLookup: suspend (String?) -> dev.app.arya.myexpence.domain.Category?): Flow<List<Budget>> =
        budgetDao.observeAll().map { list ->
            list.map { it.toDomain(categoryLookup) }
        }

    suspend fun createMonthlyBudget(
        monthStartEpochMs: Long,
        categoryId: String?,
        limitMinor: Long,
        currency: String,
        thresholds: List<Double>,
        nowEpochMs: Long,
    ): String {
        val id = UUID.randomUUID().toString()
        budgetDao.insert(
            BudgetEntity(
                id = id,
                monthStartEpochMs = monthStartEpochMs,
                categoryId = categoryId,
                limitMinor = limitMinor,
                currency = currency,
                thresholdsCsv = thresholds.joinToString(","),
                lastNotifiedThreshold = null,
                createdAtEpochMs = nowEpochMs,
                updatedAtEpochMs = nowEpochMs,
            )
        )
        return id
    }

    suspend fun getSpendForBudget(
        budget: BudgetEntity,
        monthStartEpochMs: Long,
        monthEndEpochMs: Long,
    ): Long {
        return transactionDao.sumForPeriodAndCategory(
            type = TransactionTypeDb.EXPENSE,
            categoryId = budget.categoryId,
            startEpochMs = monthStartEpochMs,
            endEpochMs = monthEndEpochMs,
        )
    }

    suspend fun updateLastNotifiedThreshold(budget: BudgetEntity, threshold: Double?, nowEpochMs: Long) {
        budgetDao.update(
            budget.copy(
                lastNotifiedThreshold = threshold,
                updatedAtEpochMs = nowEpochMs,
            )
        )
    }
}

private suspend fun BudgetEntity.toDomain(
    categoryLookup: suspend (String?) -> dev.app.arya.myexpence.domain.Category?,
): Budget {
    val thresholds = thresholdsCsv.split(',')
        .mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toDoubleOrNull() }
        .sorted()
    return Budget(
        id = id,
        monthStartEpochMs = monthStartEpochMs,
        category = categoryLookup(categoryId),
        limitMinor = limitMinor,
        currency = currency,
        thresholds = thresholds,
        lastNotifiedThreshold = lastNotifiedThreshold,
    )
}

