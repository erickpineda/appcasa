package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun getEventsByHogar(hogarId: String): Flow<List<Event>>
    suspend fun upsertEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun updateEventSyncTimestamp(eventId: String)
    fun startRemoteSync(hogarId: String)
}
