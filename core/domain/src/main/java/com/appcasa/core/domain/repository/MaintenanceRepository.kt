package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.MaintenanceEvent
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun getEventsPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<MaintenanceEvent>>
    fun getArchivedEventsPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<MaintenanceEvent>>
    suspend fun insertEvent(event: MaintenanceEvent): Long
    suspend fun deleteEvent(event: MaintenanceEvent)
    suspend fun unarchiveEvent(id: Long)
    suspend fun deleteAllArchivedEvents(hogarId: Long)
    suspend fun archiveOldEvents(hogarId: Long, threshold: Long)
}
