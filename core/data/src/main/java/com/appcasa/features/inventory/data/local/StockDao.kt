package com.appcasa.features.inventory.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId ORDER BY categoria ASC, nombre ASC")
    fun getStockByHogar(hogarId: Long): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId AND cantidad_actual <= cantidad_minima")
    fun getLowStockItems(hogarId: Long): Flow<List<StockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StockEntity): Long

    @Update
    suspend fun updateItem(item: StockEntity)

    @Delete
    suspend fun deleteItem(item: StockEntity)

    @Query("DELETE FROM stock")
    suspend fun deleteAll()
}
