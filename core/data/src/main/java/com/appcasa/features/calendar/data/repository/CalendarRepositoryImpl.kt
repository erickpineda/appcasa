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

  override fun getEventsByHogar(hogarId: Long): Flow<List<Event>> {
    return eventoDao.getEventosByHogar(hogarId).map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun insertEvent(event: Event): Long {
    var eventToInsert = event
    if (eventToInsert.hogarSyncId == null && eventToInsert.hogarId > 0) {
      val hogar = householdRepository.getHogarById(eventToInsert.hogarId).first()
      eventToInsert = eventToInsert.copy(hogarSyncId = hogar?.syncId)
    }
    if (eventToInsert.miembroSyncId == null) {
      eventToInsert.miembroId?.let { id ->
        if (id > 0) {
          val member = familyRepository.getMemberById(id)
          eventToInsert = eventToInsert.copy(miembroSyncId = member?.syncId)
        }
      }
    }
    if (eventToInsert.syncId == null) {
      eventToInsert = eventToInsert.copy(syncId = UUID.randomUUID().toString())
    }
    val existing = eventToInsert.syncId?.let { eventoDao.getEventoBySyncId(it) }
    if (existing != null) {
      eventToInsert = eventToInsert.copy(id = existing.id)
    }
    val id = eventoDao.insertEvento(eventToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
    syncScheduler.scheduleSync(eventToInsert.hogarId)
    return id
  }

  override suspend fun deleteEvent(event: Event) {
    eventoDao.deleteEvento(event.toEntity())
    try {
      val hogar = householdRepository.getHogarById(event.hogarId).first()
      val hSyncId = event.hogarSyncId ?: hogar?.syncId
      if (hSyncId != null) {
        remoteDataSource.deleteEvent(hSyncId, event)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    syncScheduler.scheduleSync(event.hogarId)
  }

  override suspend fun updateEventSyncTimestamp(eventId: Long) {
    eventoDao.updateSyncTimestamp(eventId, System.currentTimeMillis())
  }

  private var syncJob: Job? = null

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun startRemoteSync(hogarId: Long) {
    syncJob?.cancel()
    syncJob = syncManager.isAppInForeground
      .flatMapLatest { isInForeground ->
        if (isInForeground) {
          val hogar = householdRepository.getHogarById(hogarId).first()
          hogar?.syncId?.let { remoteDataSource.observeEvents(it) } ?: emptyFlow()
        } else {
          emptyFlow()
        }
      }
      .onEach { remoteItems ->
        remoteItems.forEach { remoteItem ->
          val existing = remoteItem.syncId?.let { eventoDao.getEventoBySyncId(it) }
          val hogar = householdRepository.getHogarById(hogarId).first()

          val eventToSave = remoteItem.copy(
            id = existing?.id ?: 0L,
            hogarId = hogarId,
            hogarSyncId = hogar?.syncId
          )

          if (existing == null || remoteItem.updatedAt > existing.updatedAt) {
            eventoDao.insertEvento(eventToSave.toEntity())
          }
        }
      }
      .catch { e -> e.printStackTrace() }
      .launchIn(appScope)
  }
}
