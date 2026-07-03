package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun getExpensesByHogar(hogarId: String): Flow<List<Expense>>
    fun getExpensesPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Expense>>
    fun getArchivedExpensesPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Expense>>
    suspend fun upsertExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteAllExpenses(hogarId: String)
    fun getTotalMonthlyExpense(hogarId: String, startOfMonth: Long): Flow<Double?>
    suspend fun unarchiveExpense(id: String)
    suspend fun archiveOldExpenses(hogarId: String, threshold: Long)
    suspend fun purgeOldExpensePhotos(hogarId: String, threshold: Long)
    suspend fun updateExpenseSyncTimestamp(expenseId: String)
    fun startRemoteSync(hogarId: String)
}
