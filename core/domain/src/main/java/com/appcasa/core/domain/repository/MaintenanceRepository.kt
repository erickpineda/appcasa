package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.MaintenanceEvent
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun getEventsPaged(hogarId: String, limit: Int, offset: Int): Flow<List<MaintenanceEvent>>
    fun getArchivedEventsPaged(hogarId: String, limit: Int, offset: Int): Flow<List<MaintenanceEvent>>
    suspend fun upsertEvent(event: MaintenanceEvent)
    suspend fun deleteEvent(event: MaintenanceEvent)
    suspend fun unarchiveEvent(id: String)
    suspend fun deleteAllArchivedEvents(hogarId: String)
    suspend fun archiveOldEvents(hogarId: String, threshold: Long)
    suspend fun updateMaintenanceSyncTimestamp(eventId: String)
    fun startRemoteSync(hogarId: String)
}
