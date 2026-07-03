package com.appcasa.features.tasks.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecompensaDao {
    @Query("SELECT * FROM recompensas WHERE hogar_id = :hogarId AND deleted_at IS NULL ORDER BY coste_puntos ASC")
    fun getRecompensasByHogar(hogarId: String): Flow<List<RecompensaEntity>>

    @Upsert
    suspend fun upsertRecompensa(recompensa: RecompensaEntity)

    @Query("UPDATE recompensas SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteRecompensa(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteRecompensa(recompensa: RecompensaEntity)

    @Query("SELECT * FROM recompensas WHERE id = :id AND deleted_at IS NULL")
    suspend fun getRecompensaById(id: String): RecompensaEntity?

    @Query("UPDATE recompensas SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)
}
