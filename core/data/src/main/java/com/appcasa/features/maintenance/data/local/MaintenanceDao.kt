package com.appcasa.features.maintenance.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM mantenimiento_hogar WHERE hogar_id = :hogarId ORDER BY fecha_realizacion DESC")
    fun getEventsByHogar(hogarId: Long): Flow<List<MaintenanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MaintenanceEntity): Long

    @Delete
    suspend fun deleteEvent(event: MaintenanceEntity)

    @Query("SELECT * FROM mantenimiento_hogar WHERE proxima_revision IS NOT NULL AND proxima_revision > :currentTime AND hogar_id = :hogarId ORDER BY proxima_revision ASC")
    fun getUpcomingRevisions(hogarId: Long, currentTime: Long): Flow<List<MaintenanceEntity>>
}
