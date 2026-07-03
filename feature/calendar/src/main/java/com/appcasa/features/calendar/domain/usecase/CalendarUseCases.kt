package com.appcasa.features.calendar.domain.usecase

import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.core.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    operator fun invoke(hogarId: String): Flow<List<Event>> {
        return repository.getEventsByHogar(hogarId)
    }
}

class AddEventUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    suspend operator fun invoke(
        hogarId: String,
        titulo: String,
        fecha: Long,
        tipo: TipoEvento = TipoEvento.OTRO,
        descripcion: String? = null
    ) {
        repository.upsertEvent(
            Event(
                hogarId = hogarId,
                titulo = titulo,
                fecha = fecha,
                tipo = tipo,
                descripcion = descripcion
            )
        )
    }
}

class DeleteEventUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    suspend operator fun invoke(event: Event) {
        repository.deleteEvent(event)
    }
}
