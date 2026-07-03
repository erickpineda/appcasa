package com.appcasa.features.finance.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.FinanceRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.Expense
import com.appcasa.core.domain.repository.FinanceRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.finance.data.mapper.toDomain
import com.appcasa.features.finance.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class FinanceRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val expenseDao: ExpenseDao,
    private val householdRepository: HouseholdRepository,
    private val remoteDataSource: FinanceRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
) : FinanceRepository {

    override fun getExpensesByHogar(hogarId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExpensesPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Expense>> {
        return expenseDao.getExpensesPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedExpensesPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Expense>> {
        return expenseDao.getArchivedExpensesPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun unarchiveExpense(id: String) {
        expenseDao.unarchiveExpense(id)
    }

    override suspend fun archiveOldExpenses(hogarId: String, threshold: Long) {
        expenseDao.archiveOldExpenses(hogarId, threshold)
    }

    override suspend fun purgeOldExpensePhotos(hogarId: String, threshold: Long) {
        expenseDao.purgeOldExpensePhotos(hogarId, threshold)
    }

    override suspend fun deleteAllExpenses(hogarId: String) {
        expenseDao.deleteAll() // O filtrar por hogar si existe la query
    }

    override fun getTotalMonthlyExpense(hogarId: String, startOfMonth: Long): Flow<Double?> {
        return expenseDao.getTotalMonthlyExpense(hogarId, startOfMonth)
    }

    override suspend fun upsertExpense(expense: Expense) {
        expenseDao.upsertExpense(expense.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(expense.hogarId)
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
        syncScheduler.scheduleSync(expense.hogarId)
    }

    override suspend fun updateExpenseSyncTimestamp(expenseId: String) {
        expenseDao.updateSyncTimestamp(expenseId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: String) {
        // TODO: Refactor in Phase 4
    }
}
