package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun getExpensesByHogar(hogarId: Long): Flow<List<Expense>>
    fun getExpensesPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Expense>>
    fun getArchivedExpensesPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense): Long
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteAllExpenses(hogarId: Long)
    fun getTotalMonthlyExpense(hogarId: Long, startOfMonth: Long): Flow<Double?>
    suspend fun unarchiveExpense(id: Long)
    suspend fun archiveOldExpenses(hogarId: Long, threshold: Long)
    suspend fun purgeOldExpensePhotos(hogarId: Long, threshold: Long)
    suspend fun updateExpenseSyncTimestamp(expenseId: Long)
    fun startRemoteSync(hogarId: Long)
}
