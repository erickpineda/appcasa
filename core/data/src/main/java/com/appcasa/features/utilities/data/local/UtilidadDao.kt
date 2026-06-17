package com.appcasa.features.utilities.data.local

import androidx.room.*
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilidadDao {
    @Query("SELECT * FROM utilidades ORDER BY orden ASC")
    fun getUtilidades(): Flow<List<UtilidadEntity>>

    @Upsert
    suspend fun insertUtilidad(utilidad: UtilidadEntity): Long

    @Update
    suspend fun updateUtilidad(utilidad: UtilidadEntity)

    @Query("DELETE FROM utilidades")
    suspend fun deleteAll()
}
