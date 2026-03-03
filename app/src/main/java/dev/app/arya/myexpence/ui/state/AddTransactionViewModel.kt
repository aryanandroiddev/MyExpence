package dev.app.arya.myexpence.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.app.arya.myexpence.data.db.CategoryDao
import dev.app.arya.myexpence.data.repo.SettingsRepository
import dev.app.arya.myexpence.data.repo.TransactionRepository
import dev.app.arya.myexpence.domain.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val merchant: String = "",
    val notes: String = "",
    val categoryId: String? = null,
    val error: String? = null,
)

class AddTransactionViewModel(
    private val repo: TransactionRepository,
    private val categoryDao: CategoryDao,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddTransactionUiState())
    val state: StateFlow<AddTransactionUiState> = _state.asStateFlow()

    fun setType(type: TransactionType) = _state.update { it.copy(type = type) }
    fun setAmount(value: String) = _state.update { it.copy(amount = value, error = null) }
    fun setMerchant(value: String) = _state.update { it.copy(merchant = value) }
    fun setNotes(value: String) = _state.update { it.copy(notes = value) }
    fun setCategoryId(value: String?) = _state.update { it.copy(categoryId = value) }

    fun save(nowEpochMs: Long = System.currentTimeMillis(), onDone: () -> Unit) {
        val current = _state.value
        val amountMinor = parseAmountToMinor(current.amount)
        if (amountMinor == null || amountMinor <= 0) {
            _state.value = current.copy(error = "Enter a valid amount")
            return
        }

        viewModelScope.launch {
            val currency = settings.defaultCurrency.first()
            repo.addManual(
                type = current.type,
                amountMinor = amountMinor,
                currency = currency,
                timestampEpochMs = nowEpochMs,
                categoryId = current.categoryId,
                merchant = current.merchant.ifBlank { null },
                notes = current.notes.ifBlank { null },
                nowEpochMs = nowEpochMs,
            )
            onDone()
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

private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}

