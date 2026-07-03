package com.appcasa.features.inventory.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId AND deleted_at IS NULL ORDER BY categoria ASC, nombre ASC")
    fun getStockByHogar(hogarId: String): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId AND deleted_at IS NULL ORDER BY categoria ASC, nombre ASC LIMIT :limit OFFSET :offset")
    fun getStockPaged(hogarId: String, limit: Int, offset: Int): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId AND cantidad_actual <= cantidad_minima AND deleted_at IS NULL")
    fun getLowStockItems(hogarId: String): Flow<List<StockEntity>>

    @Upsert
    suspend fun upsertItem(item: StockEntity)

    @Query("UPDATE stock SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteItem(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteItem(item: StockEntity)

    @Query("SELECT * FROM stock WHERE id = :id AND deleted_at IS NULL")
    suspend fun getItemById(id: String): StockEntity?

    @Query("UPDATE stock SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)

    @Query("DELETE FROM stock")
    suspend fun deleteAll()
}
