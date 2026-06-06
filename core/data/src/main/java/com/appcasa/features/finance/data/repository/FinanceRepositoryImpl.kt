package com.appcasa.features.finance.data.repository

import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.domain.model.Expense
import com.appcasa.core.domain.repository.FinanceRepository
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.finance.data.mapper.toDomain
import com.appcasa.features.finance.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FinanceRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val firestoreDataSource: FirestoreDataSource
) : FinanceRepository {

    override fun getExpensesByHogar(hogarId: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExpensesPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Expense>> {
        return expenseDao.getExpensesPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedExpensesPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Expense>> {
        return expenseDao.getArchivedExpensesPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertExpense(expense: Expense): Long {
        val id = expenseDao.insertExpense(expense.toEntity())
        firestoreDataSource.syncExpense(expense.copy(id = id))
        return id
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
    }

    override suspend fun deleteAllExpenses(hogarId: Long) {
        expenseDao.deleteAllArchivedExpenses(hogarId)
    }

    override fun getTotalMonthlyExpense(hogarId: Long, startOfMonth: Long): Flow<Double?> {
        return expenseDao.getTotalMonthlyExpense(hogarId, startOfMonth)
    }

    override suspend fun unarchiveExpense(id: Long) {
        expenseDao.unarchiveExpense(id)
    }

    override suspend fun archiveOldExpenses(hogarId: Long, threshold: Long) {
        expenseDao.archiveOldExpenses(hogarId, threshold)
    }

    override suspend fun purgeOldExpensePhotos(hogarId: Long, threshold: Long) {
        expenseDao.purgeOldExpensePhotos(hogarId, threshold)
    }
}
