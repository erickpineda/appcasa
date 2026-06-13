package com.appcasa.features.maintenance.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.MaintenanceRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.MaintenanceEvent
import com.appcasa.core.domain.repository.MaintenanceRepository
import com.appcasa.features.maintenance.data.local.MaintenanceDao
import com.appcasa.features.maintenance.data.mapper.toDomain
import com.appcasa.features.maintenance.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class MaintenanceRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val maintenanceDao: MaintenanceDao,
    private val remoteDataSource: MaintenanceRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
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
        val id = maintenanceDao.insertEvent(event.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(event.hogarId)
        return id
    }

    override suspend fun deleteEvent(event: MaintenanceEvent) {
        maintenanceDao.deleteEvent(event.toEntity())
        try {
            remoteDataSource.deleteMaintenance(event)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncScheduler.scheduleSync(event.hogarId)
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

    override suspend fun updateMaintenanceSyncTimestamp(eventId: Long) {
        maintenanceDao.updateSyncTimestamp(eventId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    remoteDataSource.observeMaintenance(hogarId)
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteItems ->
                val remoteIds = remoteItems.map { it.id }.toSet()
                val localItems = maintenanceDao.getEventsPaged(hogarId, 1000, 0).first()
                
                localItems.forEach { local ->
                    if (local.id !in remoteIds) {
                        maintenanceDao.deleteEvent(local)
                    }
                }

                remoteItems.forEach { remoteEvent ->
                    val localEvent = maintenanceDao.getEventById(remoteEvent.id)
                    if (localEvent == null || remoteEvent.updatedAt > localEvent.updatedAt) {
                        maintenanceDao.insertEvent(remoteEvent.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
