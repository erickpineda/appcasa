package com.appcasa.features.reminders.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {
    @Query("SELECT * FROM recordatorios WHERE hogar_id = :hogarId ORDER BY fecha_hora ASC")
    fun getRecordatoriosByHogar(hogarId: Long): Flow<List<RecordatorioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordatorio(recordatorio: RecordatorioEntity): Long

    @Update
    suspend fun updateRecordatorio(recordatorio: RecordatorioEntity)

    @Delete
    suspend fun deleteRecordatorio(recordatorio: RecordatorioEntity)

    @Query("DELETE FROM recordatorios")
    suspend fun deleteAll()
}
