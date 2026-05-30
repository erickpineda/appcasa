package com.appcasa.features.finance.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM gastos WHERE hogar_id = :hogarId ORDER BY fecha DESC")
    fun getExpensesByHogar(hogarId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(importe) FROM gastos WHERE hogar_id = :hogarId AND fecha >= :startOfMonth")
    fun getTotalMonthlyExpense(hogarId: Long, startOfMonth: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM gastos")
    suspend fun deleteAll()
}
