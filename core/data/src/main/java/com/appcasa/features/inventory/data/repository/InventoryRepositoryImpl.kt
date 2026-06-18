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

    override fun getStockByHogar(hogarId: Long): Flow<List<StockItem>> {
        return stockDao.getStockByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getStockPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<StockItem>> {
        return stockDao.getStockPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLowStockItems(hogarId: Long): Flow<List<StockItem>> {
        return stockDao.getLowStockItems(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertStockItem(item: StockItem): Long {
        var itemToInsert = item
        if (itemToInsert.hogarSyncId == null && itemToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(itemToInsert.hogarId).first()
            itemToInsert = itemToInsert.copy(hogarSyncId = hogar?.syncId)
        }
        if (itemToInsert.syncId == null) {
            itemToInsert = itemToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = itemToInsert.syncId?.let { stockDao.getItemBySyncId(it) }
        if (existing != null) {
            itemToInsert = itemToInsert.copy(id = existing.id)
        }
        val id = stockDao.insertItem(itemToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(itemToInsert.hogarId)
        return id
    }

    override suspend fun deleteStockItem(item: StockItem) {
        stockDao.deleteItem(item.toEntity())
        try {
            val hogar = householdRepository.getHogarById(item.hogarId).first()
            val hSyncId = item.hogarSyncId ?: hogar?.syncId
            if (hSyncId != null) {
                remoteDataSource.deleteStock(hSyncId, item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncScheduler.scheduleSync(item.hogarId)
    }

    override suspend fun updateStockSyncTimestamp(itemId: Long) {
        stockDao.updateSyncTimestamp(itemId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeStock(it) } ?: emptyFlow()
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val existing = remoteItem.syncId?.let { stockDao.getItemBySyncId(it) }
                    val hogar = householdRepository.getHogarById(hogarId).first()

                    val itemToSave = remoteItem.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId
                    )

                    if (existing == null || remoteItem.updatedAt > existing.updatedAt) {
                        stockDao.insertItem(itemToSave.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
