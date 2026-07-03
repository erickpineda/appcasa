package com.appcasa.features.calendar.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    @Query("SELECT * FROM eventos WHERE hogar_id = :hogarId AND deleted_at IS NULL")
    fun getEventosByHogar(hogarId: String): Flow<List<EventoEntity>>

    @Upsert
    suspend fun upsertEvento(evento: EventoEntity)

    @Query("SELECT * FROM eventos WHERE id = :id AND deleted_at IS NULL")
    suspend fun getEventoById(id: String): EventoEntity?

    @Query("UPDATE eventos SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)

    @Query("UPDATE eventos SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteEvento(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteEvento(evento: EventoEntity)

    @Query("DELETE FROM eventos")
    suspend fun deleteAll()
}
