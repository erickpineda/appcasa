package com.appcasa.features.utilities.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilidadDao {
    @Query("SELECT * FROM utilidades ORDER BY orden ASC")
    fun getUtilidades(): Flow<List<UtilidadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUtilidad(utilidad: UtilidadEntity): Long

    @Update
    suspend fun updateUtilidad(utilidad: UtilidadEntity)

    @Query("DELETE FROM utilidades")
    suspend fun deleteAll()
}
