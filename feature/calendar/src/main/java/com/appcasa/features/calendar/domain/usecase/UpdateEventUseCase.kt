package com.appcasa.features.calendar.domain.usecase

import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.repository.CalendarRepository
import com.appcasa.core.domain.scheduler.ReminderScheduler
import javax.inject.Inject

class UpdateEventUseCase @Inject constructor(
    private val repository: CalendarRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(event: Event) {
        repository.insertEvent(event)
        if (event.fecha > System.currentTimeMillis()) {
            reminderScheduler.scheduleReminder(
              id = (event.id + 10000).toInt(),
              title = "Evento: ${event.titulo}",
              message = "Hoy tienes este evento programado",
              timeInMillis = event.fecha
            )
        }
    }
}
