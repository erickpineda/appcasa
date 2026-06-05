package com.appcasa.features.maintenance.data.repository

import com.appcasa.core.domain.model.MaintenanceEvent
import com.appcasa.core.domain.repository.MaintenanceRepository
import com.appcasa.features.maintenance.data.local.MaintenanceDao
import com.appcasa.features.maintenance.data.mapper.toDomain
import com.appcasa.features.maintenance.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MaintenanceRepositoryImpl @Inject constructor(
    private val maintenanceDao: MaintenanceDao
) : MaintenanceRepository {

    override fun getEventsPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<MaintenanceEvent>> {
        return maintenanceDao.getEventsPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedEventsPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<MaintenanceEvent>> {
        return maintenanceDao.getArchivedEventsPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertEvent(event: MaintenanceEvent): Long {
        return maintenanceDao.insertEvent(event.toEntity())
    }

    override suspend fun deleteEvent(event: MaintenanceEvent) {
        maintenanceDao.deleteEvent(event.toEntity())
    }

    override suspend fun unarchiveEvent(id: Long) {
        maintenanceDao.unarchiveEvent(id)
    }

    override suspend fun deleteAllArchivedEvents(hogarId: Long) {
        maintenanceDao.deleteAllArchivedMaintenanceEvents(hogarId)
    }

    override suspend fun archiveOldEvents(hogarId: Long, threshold: Long) {
        maintenanceDao.archiveOldMaintenanceEvents(hogarId, threshold)
    }
}
