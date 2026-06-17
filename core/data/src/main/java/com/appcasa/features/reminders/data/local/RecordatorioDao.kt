package com.appcasa.features.reminders.data.local

import androidx.room.*
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {
    @Query("SELECT * FROM recordatorios WHERE hogar_id = :hogarId ORDER BY fecha_hora ASC")
    fun getRecordatoriosByHogar(hogarId: Long): Flow<List<RecordatorioEntity>>

    @Upsert
    suspend fun insertRecordatorio(recordatorio: RecordatorioEntity): Long

    @Update
    suspend fun updateRecordatorio(recordatorio: RecordatorioEntity)

    @Delete
    suspend fun deleteRecordatorio(recordatorio: RecordatorioEntity)

    @Query("DELETE FROM recordatorios")
    suspend fun deleteAll()

    @Query("SELECT * FROM recordatorios WHERE activo = 1 AND notificado = 0 AND fecha_hora > :now")
    suspend fun getAllFutureReminders(now: Long): List<RecordatorioEntity>
}
