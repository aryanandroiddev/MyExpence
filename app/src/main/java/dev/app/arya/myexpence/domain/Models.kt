package dev.app.arya.myexpence.domain

enum class TransactionType { INCOME, EXPENSE }
enum class TransactionSource { MANUAL, SMS, CSV }

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amountMinor: Long,
    val currency: String,
    val timestampEpochMs: Long,
    val category: Category?,
    val merchant: String?,
    val notes: String?,
    val source: TransactionSource,
)

data class Category(
    val id: String,
    val name: String,
    val isSystem: Boolean,
)

data class Budget(
    val id: String,
    val monthStartEpochMs: Long,
    val category: Category?, // null => overall
    val limitMinor: Long,
    val currency: String,
    val thresholds: List<Double>,
    val lastNotifiedThreshold: Double?,
)

data class DashboardSummary(
    val monthStartEpochMs: Long,
    val currency: String,
    val incomeTotalMinor: Long,
    val expenseTotalMinor: Long,
    val netMinor: Long,
    val expenseByCategory: List<CategorySpend>,
)

data class CategorySpend(
    val category: Category?,
    val totalMinor: Long,
)

