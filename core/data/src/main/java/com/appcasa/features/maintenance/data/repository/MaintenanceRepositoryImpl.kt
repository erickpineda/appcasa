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
        var eventToInsert = event
        if (eventToInsert.hogarSyncId == null && eventToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(eventToInsert.hogarId).first()
            eventToInsert = eventToInsert.copy(hogarSyncId = hogar?.syncId)
        }
        if (eventToInsert.syncId == null) {
            eventToInsert = eventToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = eventToInsert.syncId?.let { maintenanceDao.getEventBySyncId(it) }
        if (existing != null) {
            eventToInsert = eventToInsert.copy(id = existing.id)
        }
        val id = maintenanceDao.insertEvent(eventToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(eventToInsert.hogarId)
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
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeMaintenance(it) } ?: emptyFlow()
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteEvent ->
                    val existing = remoteEvent.syncId?.let { maintenanceDao.getEventBySyncId(it) }
                    val hogar = householdRepository.getHogarById(hogarId).first()

                    val eventToSave = remoteEvent.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId
                    )

                    if (existing == null || remoteEvent.updatedAt > existing.updatedAt) {
                        maintenanceDao.insertEvent(eventToSave.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
