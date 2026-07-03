package com.appcasa.features.reminders.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {
    @Query("SELECT * FROM recordatorios WHERE hogar_id = :hogarId AND deleted_at IS NULL ORDER BY fecha_hora ASC")
    fun getRecordatoriosByHogar(hogarId: String): Flow<List<RecordatorioEntity>>

    @Upsert
    suspend fun upsertRecordatorio(recordatorio: RecordatorioEntity)

    @Query("UPDATE recordatorios SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteRecordatorio(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteRecordatorio(recordatorio: RecordatorioEntity)

    @Query("DELETE FROM recordatorios")
    suspend fun deleteAll()

    @Query("SELECT * FROM recordatorios WHERE activo = 1 AND notificado = 0 AND fecha_hora > :now AND deleted_at IS NULL")
    suspend fun getAllFutureReminders(now: Long): List<RecordatorioEntity>

    @Query("SELECT * FROM recordatorios WHERE id = :id AND deleted_at IS NULL")
    suspend fun getRecordatorioById(id: String): RecordatorioEntity?

    @Query("UPDATE recordatorios SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)
}
