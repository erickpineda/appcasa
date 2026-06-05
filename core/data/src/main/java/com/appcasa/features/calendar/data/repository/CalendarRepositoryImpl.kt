package com.appcasa.features.calendar.data.repository

import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.repository.CalendarRepository
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.mapper.toDomain
import com.appcasa.features.calendar.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val eventoDao: EventoDao
) : CalendarRepository {

    override fun getEventsByHogar(hogarId: Long): Flow<List<Event>> {
        return eventoDao.getEventosByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertEvent(event: Event): Long {
        return eventoDao.insertEvento(event.toEntity())
    }

    override suspend fun deleteEvent(event: Event) {
        eventoDao.deleteEvento(event.toEntity())
    }
}
