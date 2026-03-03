package dev.app.arya.myexpence.data.repo

import dev.app.arya.myexpence.data.db.CategoryDao
import dev.app.arya.myexpence.data.db.CategoryEntity
import dev.app.arya.myexpence.data.db.TransactionDao
import dev.app.arya.myexpence.data.db.TransactionEntity
import dev.app.arya.myexpence.data.db.TransactionSourceDb
import dev.app.arya.myexpence.data.db.TransactionTypeDb
import dev.app.arya.myexpence.domain.Category
import dev.app.arya.myexpence.domain.Transaction
import dev.app.arya.myexpence.domain.TransactionSource
import dev.app.arya.myexpence.domain.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) {
    fun observeAll(): Flow<List<Transaction>> =
        combine(transactionDao.observeAll(), categoryDao.observeAll()) { txs, cats ->
            val catMap = cats.associateBy { it.id }
            txs.map { it.toDomain(catMap[it.categoryId]) }
        }

    suspend fun addManual(
        type: TransactionType,
        amountMinor: Long,
        currency: String,
        timestampEpochMs: Long,
        categoryId: String?,
        merchant: String?,
        notes: String?,
        nowEpochMs: Long,
    ): String {
        val id = UUID.randomUUID().toString()
        transactionDao.insert(
            TransactionEntity(
                id = id,
                type = type.toDb(),
                amountMinor = amountMinor,
                currency = currency,
                timestampEpochMs = timestampEpochMs,
                categoryId = categoryId,
                merchant = merchant,
                notes = notes,
                source = TransactionSourceDb.MANUAL,
                sourceKey = null,
                rawSourceJson = null,
                createdAtEpochMs = nowEpochMs,
                updatedAtEpochMs = nowEpochMs,
            )
        )
        return id
    }

    suspend fun update(
        id: String,
        type: TransactionType,
        amountMinor: Long,
        currency: String,
        timestampEpochMs: Long,
        categoryId: String?,
        merchant: String?,
        notes: String?,
        source: TransactionSource,
        sourceKey: String?,
        rawSourceJson: String?,
        createdAtEpochMs: Long,
        nowEpochMs: Long,
    ) {
        transactionDao.update(
            TransactionEntity(
                id = id,
                type = type.toDb(),
                amountMinor = amountMinor,
                currency = currency,
                timestampEpochMs = timestampEpochMs,
                categoryId = categoryId,
                merchant = merchant,
                notes = notes,
                source = source.toDb(),
                sourceKey = sourceKey,
                rawSourceJson = rawSourceJson,
                createdAtEpochMs = createdAtEpochMs,
                updatedAtEpochMs = nowEpochMs,
            )
        )
    }

    suspend fun delete(id: String) = transactionDao.deleteById(id)

    suspend fun getEntityById(id: String): TransactionEntity? = transactionDao.getById(id)
}

private fun TransactionEntity.toDomain(categoryEntity: CategoryEntity?): Transaction =
    Transaction(
        id = id,
        type = type.toDomain(),
        amountMinor = amountMinor,
        currency = currency,
        timestampEpochMs = timestampEpochMs,
        category = categoryEntity?.toDomain(),
        merchant = merchant,
        notes = notes,
        source = source.toDomain(),
    )

private fun CategoryEntity.toDomain() = Category(id = id, name = name, isSystem = isSystem)

private fun TransactionType.toDb() = when (this) {
    TransactionType.INCOME -> TransactionTypeDb.INCOME
    TransactionType.EXPENSE -> TransactionTypeDb.EXPENSE
}

private fun TransactionTypeDb.toDomain() = when (this) {
    TransactionTypeDb.INCOME -> TransactionType.INCOME
    TransactionTypeDb.EXPENSE -> TransactionType.EXPENSE
}

private fun TransactionSource.toDb() = when (this) {
    TransactionSource.MANUAL -> TransactionSourceDb.MANUAL
    TransactionSource.SMS -> TransactionSourceDb.SMS
    TransactionSource.CSV -> TransactionSourceDb.CSV
}

private fun TransactionSourceDb.toDomain() = when (this) {
    TransactionSourceDb.MANUAL -> TransactionSource.MANUAL
    TransactionSourceDb.SMS -> TransactionSource.SMS
    TransactionSourceDb.CSV -> TransactionSource.CSV
}

