package com.appcasa.features.calendar.data.local

import androidx.room.*
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    @Query("SELECT * FROM eventos WHERE hogar_id = :hogarId")
    fun getEventosByHogar(hogarId: Long): Flow<List<EventoEntity>>

    @Upsert
    suspend fun insertEvento(evento: EventoEntity): Long

    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun getEventoById(id: Long): EventoEntity?

    @Query("SELECT * FROM eventos WHERE sync_id = :syncId LIMIT 1")
    suspend fun getEventoBySyncId(syncId: String): EventoEntity?

    @Query("UPDATE eventos SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: Long, timestamp: Long)

    @Update
    suspend fun updateEvento(evento: EventoEntity)

    @Delete
    suspend fun deleteEvento(evento: EventoEntity)

    @Query("DELETE FROM eventos")
    suspend fun deleteAll()
}
