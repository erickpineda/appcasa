package com.appcasa.features.finance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM gastos WHERE hogar_id = :hogarId AND archived = 0 ORDER BY fecha DESC")
    fun getExpensesByHogar(hogarId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM gastos WHERE hogar_id = :hogarId AND archived = 0 ORDER BY fecha DESC LIMIT :limit OFFSET :offset")
    fun getExpensesPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM gastos WHERE hogar_id = :hogarId AND archived = 1 ORDER BY fecha DESC LIMIT :limit OFFSET :offset")
    fun getArchivedExpensesPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<ExpenseEntity>>

    @Query("UPDATE gastos SET archived = 0 WHERE id = :id")
    suspend fun unarchiveExpense(id: Long)

    @Query("UPDATE gastos SET archived = 1 WHERE hogar_id = :hogarId AND fecha < :threshold")
    suspend fun archiveOldExpenses(hogarId: Long, threshold: Long)

    @Query("UPDATE gastos SET foto_uri = NULL WHERE hogar_id = :hogarId AND fecha < :threshold")
    suspend fun purgeOldExpensePhotos(hogarId: Long, threshold: Long)

    @Query("DELETE FROM gastos WHERE hogar_id = :hogarId AND archived = 1")
    suspend fun deleteAllArchivedExpenses(hogarId: Long)

    @Query("SELECT SUM(importe) FROM gastos WHERE hogar_id = :hogarId AND fecha >= :startOfMonth")
    fun getTotalMonthlyExpense(hogarId: Long, startOfMonth: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM gastos")
    suspend fun deleteAll()
}
