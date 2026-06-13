package com.appcasa.features.calendar.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.CalendarRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.repository.CalendarRepository
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.mapper.toDomain
import com.appcasa.features.calendar.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
  @ApplicationScope private val appScope: CoroutineScope,
  private val eventoDao: EventoDao,
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
    val id = eventoDao.insertEvento(event.copy(updatedAt = System.currentTimeMillis()).toEntity())
    syncScheduler.scheduleSync(event.hogarId)
    return id
  }

  override suspend fun deleteEvent(event: Event) {
    eventoDao.deleteEvento(event.toEntity())
    try {
      remoteDataSource.deleteEvent(event)
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
          remoteDataSource.observeEvents(hogarId)
        } else {
          emptyFlow()
        }
      }
      .onEach { remoteItems ->
        val remoteIds = remoteItems.map { it.id }.toSet()
        val localItems = eventoDao.getEventosByHogar(hogarId).first()

        localItems.forEach { local ->
          if (local.id !in remoteIds) {
            eventoDao.deleteEvento(local)
          }
        }

        remoteItems.forEach { remoteItem ->
          val localItem = eventoDao.getEventoById(remoteItem.id)
          if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
            eventoDao.insertEvento(remoteItem.toEntity())
          }
        }
      }
      .catch { e -> e.printStackTrace() }
      .launchIn(appScope)
  }
}
