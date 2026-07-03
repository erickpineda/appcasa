package com.appcasa.features.inventory.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.InventoryRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.core.domain.repository.InventoryRepository
import com.appcasa.features.inventory.data.local.StockDao
import com.appcasa.features.inventory.data.mapper.toDomain
import com.appcasa.features.inventory.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class InventoryRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val stockDao: StockDao,
    private val householdRepository: HouseholdRepository,
    private val remoteDataSource: InventoryRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
) : InventoryRepository {

    override fun getStockByHogar(hogarId: String): Flow<List<StockItem>> {
        return stockDao.getStockByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getStockPaged(hogarId: String, limit: Int, offset: Int): Flow<List<StockItem>> {
        return stockDao.getStockPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLowStockItems(hogarId: String): Flow<List<StockItem>> {
        return stockDao.getLowStockItems(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertStockItem(item: StockItem) {
        stockDao.upsertItem(item.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(item.hogarId)
    }

    override suspend fun deleteStockItem(item: StockItem) {
        stockDao.deleteItem(item.toEntity())
        syncScheduler.scheduleSync(item.hogarId)
    }

    override suspend fun updateStockSyncTimestamp(itemId: String) {
        stockDao.updateSyncTimestamp(itemId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: String) {
        // TODO: Refactor in Phase 4
    }

    private val itemSyncJobs = mutableMapOf<String, Job>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncItems(hogarId: String, inventoryId: String) {
        // TODO: Refactor in Phase 4
    }
}
