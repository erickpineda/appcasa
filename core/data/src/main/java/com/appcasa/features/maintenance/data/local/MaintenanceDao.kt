package com.appcasa.features.maintenance.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM mantenimiento_hogar WHERE hogar_id = :hogarId AND archived = 0 AND deleted_at IS NULL ORDER BY fecha_realizacion DESC")
    fun getEventsByHogar(hogarId: String): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM mantenimiento_hogar WHERE hogar_id = :hogarId AND archived = 0 AND deleted_at IS NULL ORDER BY fecha_realizacion DESC LIMIT :limit OFFSET :offset")
    fun getEventsPaged(hogarId: String, limit: Int, offset: Int): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM mantenimiento_hogar WHERE hogar_id = :hogarId AND archived = 1 AND deleted_at IS NULL ORDER BY fecha_realizacion DESC LIMIT :limit OFFSET :offset")
    fun getArchivedEventsPaged(hogarId: String, limit: Int, offset: Int): Flow<List<MaintenanceEntity>>

    @Query("UPDATE mantenimiento_hogar SET archived = 0 WHERE id = :id")
    suspend fun unarchiveEvent(id: String)

    @Query("UPDATE mantenimiento_hogar SET archived = 1 WHERE hogar_id = :hogarId AND fecha_realizacion < :threshold AND deleted_at IS NULL")
    suspend fun archiveOldMaintenanceEvents(hogarId: String, threshold: Long)

    @Query("UPDATE mantenimiento_hogar SET deleted_at = :timestamp, deleted_by = :userId WHERE hogar_id = :hogarId AND archived = 1 AND deleted_at IS NULL")
    suspend fun softDeleteAllArchivedMaintenanceEvents(hogarId: String, timestamp: Long, userId: String)

    @Upsert
    suspend fun upsertEvent(event: MaintenanceEntity)

    @Query("UPDATE mantenimiento_hogar SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteEvent(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteEvent(event: MaintenanceEntity)

    @Query("SELECT * FROM mantenimiento_hogar WHERE proxima_revision IS NOT NULL AND proxima_revision > :currentTime AND hogar_id = :hogarId AND deleted_at IS NULL ORDER BY proxima_revision ASC")
    fun getUpcomingRevisions(hogarId: String, currentTime: Long): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM mantenimiento_hogar WHERE id = :id AND deleted_at IS NULL")
    suspend fun getEventById(id: String): MaintenanceEntity?

    @Query("UPDATE mantenimiento_hogar SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)
}
