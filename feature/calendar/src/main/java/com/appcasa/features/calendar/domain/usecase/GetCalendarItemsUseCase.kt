package com.appcasa.features.calendar.domain.usecase

import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

sealed class CalendarItem {
    abstract val timestamp: Long
    abstract val title: String

    data class EventItem(val event: Event) : CalendarItem() {
        override val timestamp = event.fecha
        override val title = event.titulo
    }
    data class TaskItem(val task: Task) : CalendarItem() {
        override val timestamp = task.fechaLimite ?: 0L
        override val title = task.titulo
    }
    data class ReminderItem(val reminder: Reminder) : CalendarItem() {
        override val timestamp = reminder.fechaHora
        override val title = reminder.titulo
    }
}

data class CalendarState(
    val upcoming: List<CalendarItem> = emptyList(),
    val history: List<CalendarItem> = emptyList(),
    val hasArchive: Boolean = false,
    val rawEvents: List<Event> = emptyList(),
    val rawTasks: List<Task> = emptyList(),
    val rawReminders: List<Reminder> = emptyList()
)

class GetCalendarItemsUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val tasksRepository: TasksRepository,
    private val reminderRepository: ReminderRepository,
    private val familyRepository: FamilyRepository
) {
    operator fun invoke(hogarId: Long, query: String = ""): Flow<CalendarState> {
        return combine(
            calendarRepository.getEventsByHogar(hogarId),
            tasksRepository.getTasksByHogar(hogarId),
            reminderRepository.getRemindersByHogar(hogarId),
            familyRepository.getMembersByHogar(hogarId)
        ) { events, tasks, reminders, members ->
            val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val archiveThreshold = LocalDate.now().minusMonths(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            val birthdayItems = members.filter { it.fechaNacimiento != null }.map { member ->
              val bdayMillis = calculateBirthdayOccurrence(member.fechaNacimiento!!)
              CalendarItem.EventItem(
                Event(
                  id = -member.id,
                  hogarId = member.hogarId,
                  titulo = "Cumpleaños: ${member.nombre} 🎂",
                  fecha = bdayMillis,
                  tipo = TipoEvento.CUMPLEANOS
                )
              )
            }

            var allItems = (events.map { CalendarItem.EventItem(it) } +
                   tasks.filter { it.fechaLimite != null }.map { CalendarItem.TaskItem(it) } +
                   reminders.map { CalendarItem.ReminderItem(it) } +
                   birthdayItems)
                   .sortedBy { it.timestamp }

            if (query.isNotBlank()) {
              allItems = allItems.filter { it.title.contains(query, ignoreCase = true) }
            }

            val historyAll = allItems.filter { it.timestamp < startOfToday }.reversed()
            
            val historyVisible = if (query.isBlank()) {
                historyAll.filter { it.timestamp >= archiveThreshold }
            } else {
                historyAll
            }

            CalendarState(
              upcoming = allItems.filter { it.timestamp >= startOfToday },
              history = historyVisible,
              hasArchive = query.isBlank() && historyAll.any { it.timestamp < archiveThreshold },
              rawEvents = events,
              rawTasks = tasks.filter { it.fechaLimite != null },
              rawReminders = reminders
            )
        }
    }

    private fun calculateBirthdayOccurrence(birthDateMillis: Long): Long {
        val birthDate = Calendar.getInstance().apply { timeInMillis = birthDateMillis }
        val today = Calendar.getInstance()
        
        val occurrence = Calendar.getInstance().apply {
          set(Calendar.YEAR, today.get(Calendar.YEAR))
          set(Calendar.MONTH, birthDate.get(Calendar.MONTH))
          set(Calendar.DAY_OF_MONTH, birthDate.get(Calendar.DAY_OF_MONTH))
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        return occurrence.timeInMillis
    }
}
