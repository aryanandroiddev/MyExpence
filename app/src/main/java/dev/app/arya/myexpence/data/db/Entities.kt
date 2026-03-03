package dev.app.arya.myexpence.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionTypeDb { INCOME, EXPENSE }
enum class TransactionSourceDb { MANUAL, SMS, CSV }

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["sourceKey"], unique = true),
        Index(value = ["timestampEpochMs"]),
        Index(value = ["categoryId"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: TransactionTypeDb,
    val amountMinor: Long,
    val currency: String,
    val timestampEpochMs: Long,
    val categoryId: String?,
    val merchant: String?,
    val notes: String?,
    val source: TransactionSourceDb,
    val sourceKey: String?,
    val rawSourceJson: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isSystem: Boolean,
    val createdAtEpochMs: Long,
)

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["monthStartEpochMs", "categoryId"], unique = true),
    ],
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val monthStartEpochMs: Long,
    val categoryId: String?, // null => overall
    val limitMinor: Long,
    val currency: String,
    val thresholdsCsv: String, // e.g. "0.8,1.0"
    val lastNotifiedThreshold: Double?, // to prevent spam
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@Entity(tableName = "ingestion_state")
data class IngestionStateEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAtEpochMs: Long,
)

