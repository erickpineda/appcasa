package com.appcasa.features.calendar.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    @Query("SELECT * FROM eventos WHERE hogar_id = :hogarId")
    fun getEventosByHogar(hogarId: Long): Flow<List<EventoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvento(evento: EventoEntity): Long

    @Update
    suspend fun updateEvento(evento: EventoEntity)

    @Delete
    suspend fun deleteEvento(evento: EventoEntity)

    @Query("DELETE FROM eventos")
    suspend fun deleteAll()
}
