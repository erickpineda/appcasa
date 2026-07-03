package com.appcasa.features.maintenance.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.MaintenanceRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.MaintenanceEvent
import com.appcasa.core.domain.repository.MaintenanceRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.features.maintenance.data.local.MaintenanceDao
import com.appcasa.features.maintenance.data.mapper.toDomain
import com.appcasa.features.maintenance.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class MaintenanceRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val maintenanceDao: MaintenanceDao,
    private val householdRepository: HouseholdRepository,
    private val remoteDataSource: MaintenanceRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
) : MaintenanceRepository {

    override fun getEventsPaged(hogarId: String, limit: Int, offset: Int): Flow<List<MaintenanceEvent>> {
        return maintenanceDao.getEventsPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedEventsPaged(hogarId: String, limit: Int, offset: Int): Flow<List<MaintenanceEvent>> {
        return maintenanceDao.getArchivedEventsPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertEvent(event: MaintenanceEvent) {
        maintenanceDao.upsertEvent(event.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(event.hogarId)
    }

    override suspend fun deleteEvent(event: MaintenanceEvent) {
        maintenanceDao.deleteEvent(event.toEntity())
        syncScheduler.scheduleSync(event.hogarId)
    }

    override suspend fun unarchiveEvent(id: String) {
        maintenanceDao.unarchiveEvent(id)
    }

    override suspend fun deleteAllArchivedEvents(hogarId: String) {
        maintenanceDao.softDeleteAllArchivedMaintenanceEvents(hogarId, System.currentTimeMillis(), "system")
    }

    override suspend fun archiveOldEvents(hogarId: String, threshold: Long) {
        maintenanceDao.archiveOldMaintenanceEvents(hogarId, threshold)
    }

    override suspend fun updateMaintenanceSyncTimestamp(eventId: String) {
        maintenanceDao.updateSyncTimestamp(eventId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: String) {
        // TODO: Refactor in Phase 4
    }
}
