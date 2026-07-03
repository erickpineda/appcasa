package com.appcasa.features.maintenance.domain.usecase

import com.appcasa.core.domain.model.MaintenanceEvent
import com.appcasa.core.domain.repository.MaintenanceRepository
import com.appcasa.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMaintenanceEventsUseCase @Inject constructor(
  private val repository: MaintenanceRepository
) {
  operator fun invoke(hogarId: String, page: Int): Flow<List<MaintenanceEvent>> {
    return repository.getEventsPaged(hogarId, limit = page * 20, offset = 0)
  }
}

class GetArchivedMaintenanceEventsUseCase @Inject constructor(
  private val repository: MaintenanceRepository
) {
  operator fun invoke(hogarId: String, page: Int): Flow<List<MaintenanceEvent>> {
    return repository.getArchivedEventsPaged(hogarId, limit = page * 20, offset = 0)
  }
}

class AddMaintenanceEventUseCase @Inject constructor(
  private val repository: MaintenanceRepository,
  private val reminderScheduler: ReminderScheduler
) {
  suspend operator fun invoke(
    hogarId: String,
    title: String,
    cat: String,
    desc: String?,
    date: Long,
    nextDate: Long?,
    cost: Double?
  ) {
    val id = java.util.UUID.randomUUID().toString()
    val event = MaintenanceEvent(
      id = id,
      hogarId = hogarId,
      titulo = title,
      categoria = cat,
      descripcion = desc,
      fechaRealizacion = date,
      proximaRevision = nextDate,
      coste = cost
    )
    repository.upsertEvent(event)

    nextDate?.let { revision ->
      val remindTime = revision - (7L * 24 * 60 * 60 * 1000)
      if (remindTime > System.currentTimeMillis()) {
        reminderScheduler.scheduleReminder(
          id = (id + 40000).hashCode(),
          title = "Mantenimiento: $title",
          message = "Tienes una revisión pendiente de $title la próxima semana.",
          timeInMillis = remindTime
        )
      }
    }
  }
}

class DeleteMaintenanceEventUseCase @Inject constructor(
  private val repository: MaintenanceRepository,
  private val reminderScheduler: ReminderScheduler
) {
  suspend operator fun invoke(event: MaintenanceEvent) {
    repository.deleteEvent(event)
    reminderScheduler.cancelReminder((event.id + 40000).hashCode())
  }
}

class ArchiveMaintenanceEventUseCase @Inject constructor(
  private val repository: MaintenanceRepository,
  private val reminderScheduler: ReminderScheduler
) {
  suspend operator fun invoke(event: MaintenanceEvent) {
    repository.upsertEvent(event.copy(archived = true))
    reminderScheduler.cancelReminder((event.id + 40000).hashCode())
  }
}

class UnarchiveMaintenanceEventUseCase @Inject constructor(
  private val repository: MaintenanceRepository
) {
  suspend operator fun invoke(eventId: String) {
    repository.unarchiveEvent(eventId)
  }
}

class ClearAllArchivedMaintenanceUseCase @Inject constructor(
  private val repository: MaintenanceRepository
) {
  suspend operator fun invoke(hogarId: String) {
    repository.deleteAllArchivedEvents(hogarId)
  }
}

class ArchiveOldMaintenanceEventsUseCase @Inject constructor(
  private val repository: MaintenanceRepository
) {
  suspend operator fun invoke(hogarId: String) {
    val threshold = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
    repository.archiveOldEvents(hogarId, threshold)
  }
}
