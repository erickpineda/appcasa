package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.StockItem
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getStockByHogar(hogarId: Long): Flow<List<StockItem>>
    fun getStockPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<StockItem>>
    fun getLowStockItems(hogarId: Long): Flow<List<StockItem>>
    suspend fun insertStockItem(item: StockItem): Long
    suspend fun deleteStockItem(item: StockItem)
    suspend fun updateStockSyncTimestamp(itemId: Long)
    fun startRemoteSync(hogarId: Long)
}
