package com.appcasa.features.maintenance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM mantenimiento_hogar WHERE hogar_id = :hogarId AND archived = 0 ORDER BY fecha_realizacion DESC")
    fun getEventsByHogar(hogarId: Long): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM mantenimiento_hogar WHERE hogar_id = :hogarId AND archived = 0 ORDER BY fecha_realizacion DESC LIMIT :limit OFFSET :offset")
    fun getEventsPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM mantenimiento_hogar WHERE hogar_id = :hogarId AND archived = 1 ORDER BY fecha_realizacion DESC LIMIT :limit OFFSET :offset")
    fun getArchivedEventsPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<MaintenanceEntity>>

    @Query("UPDATE mantenimiento_hogar SET archived = 0 WHERE id = :id")
    suspend fun unarchiveEvent(id: Long)

    @Query("UPDATE mantenimiento_hogar SET archived = 1 WHERE hogar_id = :hogarId AND fecha_realizacion < :threshold")
    suspend fun archiveOldMaintenanceEvents(hogarId: Long, threshold: Long)

    @Query("DELETE FROM mantenimiento_hogar WHERE hogar_id = :hogarId AND archived = 1")
    suspend fun deleteAllArchivedMaintenanceEvents(hogarId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MaintenanceEntity): Long

    @Delete
    suspend fun deleteEvent(event: MaintenanceEntity)

    @Query("SELECT * FROM mantenimiento_hogar WHERE proxima_revision IS NOT NULL AND proxima_revision > :currentTime AND hogar_id = :hogarId ORDER BY proxima_revision ASC")
    fun getUpcomingRevisions(hogarId: Long, currentTime: Long): Flow<List<MaintenanceEntity>>
}
