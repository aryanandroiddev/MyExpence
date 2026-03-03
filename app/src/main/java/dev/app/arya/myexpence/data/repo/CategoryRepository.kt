package dev.app.arya.myexpence.data.repo

import dev.app.arya.myexpence.data.db.CategoryDao
import dev.app.arya.myexpence.data.db.CategoryEntity
import dev.app.arya.myexpence.domain.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CategoryRepository(
    private val categoryDao: CategoryDao,
) {
    fun observeAll(): Flow<List<Category>> =
        categoryDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun seedDefaultsIfEmpty(nowEpochMs: Long) {
        // Simple seed strategy: always upsert a fixed set of system categories.
        val defaults = listOf(
            "Food",
            "Transport",
            "Shopping",
            "Bills",
            "Entertainment",
            "Health",
            "Education",
            "Rent",
            "Groceries",
            "Salary",
            "Other",
        ).map { name ->
            CategoryEntity(
                id = UUID.nameUUIDFromBytes("system:$name".toByteArray()).toString(),
                name = name,
                isSystem = true,
                createdAtEpochMs = nowEpochMs,
            )
        }
        categoryDao.upsertAll(defaults)
    }
}

private fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    isSystem = isSystem,
)

