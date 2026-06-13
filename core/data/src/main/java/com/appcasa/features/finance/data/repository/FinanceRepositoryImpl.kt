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

    override suspend fun unarchiveExpense(id: Long) {
        expenseDao.unarchiveExpense(id)
    }

    override suspend fun archiveOldExpenses(hogarId: Long, threshold: Long) {
        expenseDao.archiveOldExpenses(hogarId, threshold)
    }

    override suspend fun purgeOldExpensePhotos(hogarId: Long, threshold: Long) {
        expenseDao.purgeOldExpensePhotos(hogarId, threshold)
    }

    override suspend fun deleteAllExpenses(hogarId: Long) {
        expenseDao.deleteAll() // O filtrar por hogar si existe la query
    }

    override fun getTotalMonthlyExpense(hogarId: Long, startOfMonth: Long): Flow<Double?> {
        return expenseDao.getTotalMonthlyExpense(hogarId, startOfMonth)
    }

    override suspend fun insertExpense(expense: Expense): Long {
        var expenseToInsert = expense
        
        // Resolve hogarSyncId
        if (expenseToInsert.hogarSyncId == null && expenseToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(expenseToInsert.hogarId).first()
            expenseToInsert = expenseToInsert.copy(hogarSyncId = hogar?.syncId)
        }

        // Offline-first syncId
        if (expenseToInsert.syncId == null) {
            expenseToInsert = expenseToInsert.copy(syncId = UUID.randomUUID().toString())
        }

        // Avoid local duplicates from remote sync
        val existing = expenseToInsert.syncId?.let { expenseDao.getExpenseBySyncId(it) }
        if (existing != null) {
            expenseToInsert = expenseToInsert.copy(id = existing.id)
        }

        val id = expenseDao.insertExpense(expenseToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(expenseToInsert.hogarId)
        return id
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
        try {
            remoteDataSource.deleteExpense(expense)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncScheduler.scheduleSync(expense.hogarId)
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
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeExpenses(it) } ?: emptyFlow()
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteExpenses ->
                remoteExpenses.forEach { remoteExpense ->
                    val existing = remoteExpense.syncId?.let { expenseDao.getExpenseBySyncId(it) }
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    
                    val expenseToSave = remoteExpense.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId
                    )

                    if (existing == null || remoteExpense.updatedAt > existing.updatedAt) {
                        expenseDao.insertExpense(expenseToSave.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
