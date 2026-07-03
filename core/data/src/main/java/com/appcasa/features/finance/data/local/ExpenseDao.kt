package com.appcasa.features.finance.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM gastos WHERE hogar_id = :hogarId AND archived = 0 AND deleted_at IS NULL ORDER BY fecha DESC")
    fun getExpensesByHogar(hogarId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM gastos WHERE hogar_id = :hogarId AND archived = 0 AND deleted_at IS NULL ORDER BY fecha DESC LIMIT :limit OFFSET :offset")
    fun getExpensesPaged(hogarId: String, limit: Int, offset: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM gastos WHERE hogar_id = :hogarId AND archived = 1 AND deleted_at IS NULL ORDER BY fecha DESC LIMIT :limit OFFSET :offset")
    fun getArchivedExpensesPaged(hogarId: String, limit: Int, offset: Int): Flow<List<ExpenseEntity>>

    @Query("UPDATE gastos SET archived = 0 WHERE id = :id")
    suspend fun unarchiveExpense(id: String)

    @Query("UPDATE gastos SET archived = 1 WHERE hogar_id = :hogarId AND fecha < :threshold AND deleted_at IS NULL")
    suspend fun archiveOldExpenses(hogarId: String, threshold: Long)

    @Query("UPDATE gastos SET foto_uri = NULL WHERE hogar_id = :hogarId AND fecha < :threshold AND deleted_at IS NULL")
    suspend fun purgeOldExpensePhotos(hogarId: String, threshold: Long)

    @Query("DELETE FROM gastos WHERE hogar_id = :hogarId AND archived = 1")
    suspend fun deleteAllArchivedExpenses(hogarId: String)

    @Query("SELECT SUM(importe) FROM gastos WHERE hogar_id = :hogarId AND fecha >= :startOfMonth AND deleted_at IS NULL")
    fun getTotalMonthlyExpense(hogarId: String, startOfMonth: Long): Flow<Double?>

    @Upsert
    suspend fun upsertExpense(expense: ExpenseEntity)

    @Query("UPDATE gastos SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteExpense(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM gastos WHERE id = :id AND deleted_at IS NULL")
    suspend fun getExpenseById(id: String): ExpenseEntity?

    @Query("UPDATE gastos SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)

    @Query("DELETE FROM gastos")
    suspend fun deleteAll()
}
