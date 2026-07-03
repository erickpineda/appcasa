package com.appcasa.features.reminders.domain.usecase

import com.appcasa.core.domain.model.Reminder
import com.appcasa.core.domain.repository.ReminderRepository
import com.appcasa.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(hogarId: String): Flow<List<Reminder>> {
        return repository.getRemindersByHogar(hogarId)
    }
}

class AddReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(
        hogarId: String,
        title: String,
        message: String,
        dateTime: Long,
        anticipacionMins: Int = 0
    ) {
        val reminder = Reminder(
            hogarId = hogarId,
            titulo = title,
            descripcion = message,
            fechaHora = dateTime,
            activo = true
        )
        val id = repository.upsertReminder(reminder)
        
        val scheduledTime = dateTime - (anticipacionMins * 60 * 1000)
        reminderScheduler.scheduleReminder(
            id = id.hashCode(),
            title = title,
            message = if (anticipacionMins > 0) "Aviso: En $anticipacionMins minutos: $message" else message,
            timeInMillis = scheduledTime
        )
    }
}

class UpdateReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(reminder: Reminder, anticipacionMins: Int = 0) {
        repository.upsertReminder(reminder)
        if (reminder.activo) {
            val scheduledTime = reminder.fechaHora - (anticipacionMins * 60 * 1000)
            reminderScheduler.scheduleReminder(
                id = reminder.id.hashCode(),
                title = reminder.titulo,
                message = if (anticipacionMins > 0) "Aviso: En $anticipacionMins min: ${reminder.descripcion ?: ""}" else (reminder.descripcion ?: ""),
                timeInMillis = scheduledTime
            )
        } else {
            reminderScheduler.cancelReminder(reminder.id.hashCode())
        }
    }
}

class ToggleReminderActiveUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(reminder: Reminder) {
        val updated = reminder.copy(activo = !reminder.activo, updatedAt = System.currentTimeMillis())
        repository.upsertReminder(updated)
        
        if (updated.activo) {
            reminderScheduler.scheduleReminder(
                id = updated.id.hashCode(),
                title = updated.titulo,
                message = updated.descripcion ?: "",
                timeInMillis = updated.fechaHora
            )
        } else {
            reminderScheduler.cancelReminder(updated.id.hashCode())
        }
    }
}

class DeleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(reminder: Reminder) {
        repository.deleteReminder(reminder)
        reminderScheduler.cancelReminder(reminder.id.hashCode())
    }
}
