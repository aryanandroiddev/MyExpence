package dev.app.arya.myexpence.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: TransactionEntity)

    @Update
    suspend fun update(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM transactions ORDER BY timestampEpochMs DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Query(
        """
        SELECT COALESCE(SUM(amountMinor), 0)
        FROM transactions
        WHERE type = :type
          AND timestampEpochMs >= :startEpochMs
          AND timestampEpochMs < :endEpochMs
        """
    )
    suspend fun sumForPeriod(type: TransactionTypeDb, startEpochMs: Long, endEpochMs: Long): Long

    @Query(
        """
        SELECT COALESCE(SUM(amountMinor), 0)
        FROM transactions
        WHERE type = :type
          AND categoryId IS :categoryId
          AND timestampEpochMs >= :startEpochMs
          AND timestampEpochMs < :endEpochMs
        """
    )
    suspend fun sumForPeriodAndCategory(
        type: TransactionTypeDb,
        categoryId: String?,
        startEpochMs: Long,
        endEpochMs: Long,
    ): Long

    @Query(
        """
        SELECT categoryId AS categoryId, COALESCE(SUM(amountMinor), 0) AS totalMinor
        FROM transactions
        WHERE type = :type
          AND timestampEpochMs >= :startEpochMs
          AND timestampEpochMs < :endEpochMs
        GROUP BY categoryId
        ORDER BY totalMinor DESC
        """
    )
    suspend fun categoryTotalsForPeriod(
        type: TransactionTypeDb,
        startEpochMs: Long,
        endEpochMs: Long,
    ): List<CategoryTotalRow>

    data class CategoryTotalRow(
        val categoryId: String?,
        val totalMinor: Long,
    )
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: BudgetEntity)

    @Update
    suspend fun update(entity: BudgetEntity)

    @Query("SELECT * FROM budgets ORDER BY monthStartEpochMs DESC")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE monthStartEpochMs = :monthStartEpochMs")
    suspend fun getForMonth(monthStartEpochMs: Long): List<BudgetEntity>
}

@Dao
interface IngestionStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: IngestionStateEntity)

    @Query("SELECT * FROM ingestion_state WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): IngestionStateEntity?
}

