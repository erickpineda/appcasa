package com.appcasa.features.inventory.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId ORDER BY categoria ASC, nombre ASC")
    fun getStockByHogar(hogarId: Long): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId ORDER BY categoria ASC, nombre ASC LIMIT :limit OFFSET :offset")
    fun getStockPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId AND cantidad_actual <= cantidad_minima")
    fun getLowStockItems(hogarId: Long): Flow<List<StockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StockEntity): Long

    @Update
    suspend fun updateItem(item: StockEntity)

    @Delete
    suspend fun deleteItem(item: StockEntity)

    @Query("SELECT * FROM stock WHERE id = :id")
    suspend fun getItemById(id: Long): StockEntity?

    @Query("UPDATE stock SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: Long, timestamp: Long)

    @Query("DELETE FROM stock")
    suspend fun deleteAll()
}
