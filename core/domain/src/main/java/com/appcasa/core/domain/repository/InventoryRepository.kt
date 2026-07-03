package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.StockItem
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getStockByHogar(hogarId: String): Flow<List<StockItem>>
    fun getStockPaged(hogarId: String, limit: Int, offset: Int): Flow<List<StockItem>>
    fun getLowStockItems(hogarId: String): Flow<List<StockItem>>
    suspend fun upsertStockItem(item: StockItem)
    suspend fun deleteStockItem(item: StockItem)
    suspend fun updateStockSyncTimestamp(itemId: String)
    fun startRemoteSync(hogarId: String)
}
