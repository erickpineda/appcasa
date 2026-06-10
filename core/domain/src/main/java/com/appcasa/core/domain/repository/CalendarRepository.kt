package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun getEventsByHogar(hogarId: Long): Flow<List<Event>>
    suspend fun insertEvent(event: Event): Long
    suspend fun deleteEvent(event: Event)
    suspend fun updateEventSyncTimestamp(eventId: Long)
    fun startRemoteSync(hogarId: Long)
}
