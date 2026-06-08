package com.appcasa.features.inventory.data.repository

import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.repository.InventoryRepository
import com.appcasa.features.inventory.data.local.StockDao
import com.appcasa.features.inventory.data.mapper.toDomain
import com.appcasa.features.inventory.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val stockDao: StockDao,
    private val firestoreDataSource: FirestoreDataSource,
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
        val id = stockDao.insertItem(item.toEntity())
        syncScheduler.scheduleSync(item.hogarId)
        return id
    }

    override suspend fun deleteStockItem(item: StockItem) {
        stockDao.deleteItem(item.toEntity())
        syncScheduler.scheduleSync(item.hogarId)
    }

    override fun startRemoteSync(hogarId: Long) {
        firestoreDataSource.observeStock(hogarId)
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val localItem = stockDao.getItemById(remoteItem.id)
                    if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
                        stockDao.insertItem(remoteItem.toEntity())
                    }
                }
            }
            .launchIn(appScope)
    }
}
