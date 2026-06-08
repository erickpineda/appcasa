package com.appcasa.features.calendar.data.repository

import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.repository.CalendarRepository
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.mapper.toDomain
import com.appcasa.features.calendar.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val eventoDao: EventoDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val syncScheduler: SyncScheduler
) : CalendarRepository {

    override fun getEventsByHogar(hogarId: Long): Flow<List<Event>> {
        return eventoDao.getEventosByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertEvent(event: Event): Long {
        val id = eventoDao.insertEvento(event.toEntity())
        syncScheduler.scheduleSync(event.hogarId)
        return id
    }

    override suspend fun deleteEvent(event: Event) {
        eventoDao.deleteEvento(event.toEntity())
        syncScheduler.scheduleSync(event.hogarId)
    }

    override fun startRemoteSync(hogarId: Long) {
        firestoreDataSource.observeEvents(hogarId)
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val localItem = eventoDao.getEventoById(remoteItem.id)
                    if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
                        eventoDao.insertEvento(remoteItem.toEntity())
                    }
                }
            }
            .launchIn(appScope)
    }
}
