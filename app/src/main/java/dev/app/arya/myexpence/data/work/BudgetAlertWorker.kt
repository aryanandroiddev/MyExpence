package dev.app.arya.myexpence.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.app.arya.myexpence.MyExpenceApp
import dev.app.arya.myexpence.data.db.BudgetEntity
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.TimeZone

class BudgetAlertWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as MyExpenceApp
        val container = app.container
        val budgetDao = container.db.budgetDao()
        val transactionDao = container.db.transactionDao()

        val monthStart = monthStartUtc(System.currentTimeMillis())
        val monthEnd = monthEndUtc(monthStart)
        val budgets = budgetDao.getForMonth(monthStart)

        for (budget in budgets) {
            val spend = transactionDao.sumForPeriodAndCategory(
                type = dev.app.arya.myexpence.data.db.TransactionTypeDb.EXPENSE,
                categoryId = budget.categoryId,
                startEpochMs = monthStart,
                endEpochMs = monthEnd,
            )
            maybeNotifyBudget(budget, spend)
        }

        return Result.success()
    }

    private suspend fun maybeNotifyBudget(budget: BudgetEntity, spendMinor: Long) {
        if (budget.limitMinor <= 0) return
        val thresholds = budget.thresholdsCsv.split(',')
            .mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toDoubleOrNull() }
            .sorted()
        if (thresholds.isEmpty()) return

        val ratio = spendMinor.toDouble() / budget.limitMinor.toDouble()
        val crossed = thresholds.lastOrNull { ratio >= it } ?: return
        if (budget.lastNotifiedThreshold != null && crossed <= budget.lastNotifiedThreshold) return

        val title = "Budget alert"
        val scope = budget.categoryId?.let { "Category" } ?: "Overall"
        val message = "$scope spending reached ${(crossed * 100).toInt()}% of your budget."
        Notifications.showBudgetAlert(applicationContext, title, message)

        val app = applicationContext as MyExpenceApp
        app.container.budgetRepository.updateLastNotifiedThreshold(budget, crossed, System.currentTimeMillis())
    }

    companion object {
        const val UNIQUE_WORK_NAME = "budget_alerts"
    }
}

private fun monthStartUtc(nowEpochMs: Long): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = nowEpochMs
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun monthEndUtc(monthStartEpochMs: Long): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = monthStartEpochMs
    cal.add(Calendar.MONTH, 1)
    return cal.timeInMillis
}

