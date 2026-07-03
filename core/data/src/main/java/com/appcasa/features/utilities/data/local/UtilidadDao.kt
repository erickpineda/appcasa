package com.appcasa.features.utilities.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilidadDao {
    @Query("SELECT * FROM utilidades WHERE deleted_at IS NULL ORDER BY orden ASC")
    fun getUtilidades(): Flow<List<UtilidadEntity>>

    @Upsert
    suspend fun upsertUtilidad(utilidad: UtilidadEntity)

    @Query("UPDATE utilidades SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteUtilidad(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteUtilidad(utilidad: UtilidadEntity)

    @Query("DELETE FROM utilidades")
    suspend fun deleteAll()
}
