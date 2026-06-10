package com.appcasa.features.finance.data.repository

import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.Expense
import com.appcasa.core.domain.repository.FinanceRepository
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.finance.data.mapper.toDomain
import com.appcasa.features.finance.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FinanceRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val expenseDao: ExpenseDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
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
        syncScheduler.scheduleSync(expense.hogarId)
        return id
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
        syncScheduler.scheduleSync(expense.hogarId)
    }

    override suspend fun deleteAllExpenses(hogarId: Long) {
        expenseDao.deleteAllArchivedExpenses(hogarId)
        syncScheduler.scheduleSync(hogarId)
    }

    override fun getTotalMonthlyExpense(hogarId: Long, startOfMonth: Long): Flow<Double?> {
        return expenseDao.getTotalMonthlyExpense(hogarId, startOfMonth)
    }

    override suspend fun unarchiveExpense(id: Long) {
        expenseDao.unarchiveExpense(id)
        // sync if needed
    }

    override suspend fun archiveOldExpenses(hogarId: Long, threshold: Long) {
        expenseDao.archiveOldExpenses(hogarId, threshold)
        syncScheduler.scheduleSync(hogarId)
    }

    override suspend fun purgeOldExpensePhotos(hogarId: Long, threshold: Long) {
        expenseDao.purgeOldExpensePhotos(hogarId, threshold)
    }

    override suspend fun updateExpenseSyncTimestamp(expenseId: Long) {
        expenseDao.updateSyncTimestamp(expenseId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    firestoreDataSource.observeExpenses(hogarId)
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteExpenses ->
                remoteExpenses.forEach { remoteExpense ->
                    val localExpense = expenseDao.getExpenseById(remoteExpense.id)
                    if (localExpense == null || remoteExpense.updatedAt > localExpense.updatedAt) {
                        expenseDao.insertExpense(remoteExpense.toEntity())
                    }
                }
            }
            .launchIn(appScope)
    }
}
