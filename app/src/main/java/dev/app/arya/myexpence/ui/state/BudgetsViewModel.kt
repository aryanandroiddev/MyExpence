package dev.app.arya.myexpence.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.app.arya.myexpence.data.db.BudgetEntity
import dev.app.arya.myexpence.data.db.CategoryDao
import dev.app.arya.myexpence.data.repo.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

data class BudgetRow(
    val budget: BudgetEntity,
    val categoryName: String,
)

class BudgetsViewModel(
    private val repo: BudgetRepository,
    private val categoryDao: CategoryDao,
) : ViewModel() {
    val budgets: Flow<List<BudgetRow>> =
        combine(repo.observeAll(), categoryDao.observeAll()) { budgets, categories ->
            val catMap = categories.associateBy { it.id }
            budgets.map { b ->
                BudgetRow(
                    budget = b,
                    categoryName = b.categoryId?.let { catMap[it]?.name } ?: "Overall",
                )
            }
        }

    private val _createError = MutableStateFlow<String?>(null)
    val createError: StateFlow<String?> = _createError.asStateFlow()

    fun createOverallMonthlyBudget(limitAmountText: String, currency: String) {
        val limitMinor = parseAmountToMinor(limitAmountText)
        if (limitMinor == null || limitMinor <= 0) {
            _createError.value = "Enter a valid limit"
            return
        }
        _createError.value = null
        val monthStart = monthStartUtc(System.currentTimeMillis())
        viewModelScope.launch {
            repo.createMonthlyBudget(
                monthStartEpochMs = monthStart,
                categoryId = null,
                limitMinor = limitMinor,
                currency = currency,
                thresholds = listOf(0.8, 1.0),
                nowEpochMs = System.currentTimeMillis(),
            )
        }
    }
}

private fun parseAmountToMinor(text: String): Long? {
    val cleaned = text.trim().replace(",", "")
    val value = cleaned.toBigDecimalOrNull() ?: return null
    val minor = value.movePointRight(2)
    return try {
        minor.toLong()
    } catch (_: ArithmeticException) {
        null
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

