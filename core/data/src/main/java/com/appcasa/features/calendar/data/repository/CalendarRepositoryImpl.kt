package com.appcasa.features.calendar.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.CalendarRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.repository.CalendarRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.mapper.toDomain
import com.appcasa.features.calendar.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class CalendarRepositoryImpl @Inject constructor(
  @ApplicationScope private val appScope: CoroutineScope,
  private val eventoDao: EventoDao,
  private val householdRepository: HouseholdRepository,
  private val familyRepository: FamilyRepository,
  private val remoteDataSource: CalendarRemoteDataSource,
  private val syncManager: SyncManager,
  private val syncScheduler: SyncScheduler
) : CalendarRepository {

  override fun getEventsByHogar(hogarId: String): Flow<List<Event>> {
    return eventoDao.getEventosByHogar(hogarId).map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun upsertEvent(event: Event) {
    eventoDao.upsertEvento(event.copy(updatedAt = System.currentTimeMillis()).toEntity())
    syncScheduler.scheduleSync(event.hogarId)
  }

  override suspend fun deleteEvent(event: Event) {
    eventoDao.deleteEvento(event.toEntity())
    syncScheduler.scheduleSync(event.hogarId)
  }

  override suspend fun updateEventSyncTimestamp(eventId: String) {
    eventoDao.updateSyncTimestamp(eventId, System.currentTimeMillis())
  }

  private var syncJob: Job? = null

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun startRemoteSync(hogarId: String) {
    // TODO Phase 4 Remote Sync
  }
}
