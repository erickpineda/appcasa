package com.appcasa.features.inventory.data.repository

import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.repository.InventoryRepository
import com.appcasa.features.inventory.data.local.StockDao
import com.appcasa.features.inventory.data.mapper.toDomain
import com.appcasa.features.inventory.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val stockDao: StockDao
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
        return stockDao.insertItem(item.toEntity())
    }

    override suspend fun deleteStockItem(item: StockItem) {
        stockDao.deleteItem(item.toEntity())
    }
}
