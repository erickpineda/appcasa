package com.appcasa.features.calendar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.local.EventoEntity
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.reminders.data.local.RecordatorioDao
import com.appcasa.features.reminders.data.local.RecordatorioEntity
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
  private val eventoDao: EventoDao,
  private val tareaDao: TareaDao,
  private val recordatorioDao: RecordatorioDao,
  private val miembroDao: MiembroDao,
  private val reminderScheduler: ReminderScheduler,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val _historyPage = MutableStateFlow(0)
  val historyPage = _historyPage.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  val calendarItems: StateFlow<CalendarState> = combine(
    currentHouseholdProvider.householdId,
    _searchQuery
  ) { householdId, query -> 
    householdId to query
  }
    .flatMapLatest { (householdId, query) ->
      combine(
        eventoDao.getEventosByHogar(householdId),
        tareaDao.getTareasByHogar(householdId),
        recordatorioDao.getRecordatoriosByHogar(householdId),
        miembroDao.getMiembrosByHogar(householdId)
      ) { eventos, tareas, recordatorios, miembros ->
        val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val archiveThreshold = LocalDate.now().minusMonths(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val birthdayItems = miembros.filter { it.fechaNacimiento != null }.map { miembro ->
          val bdayMillis = calculateBirthdayOccurrence(miembro.fechaNacimiento!!)
          CalendarItem.Evento(
            EventoEntity(
              id = -miembro.id,
              hogarId = miembro.hogarId,
              titulo = "Cumpleaños: ${miembro.nombre} 🎂",
              fecha = bdayMillis,
              tipo = TipoEvento.CUMPLEANOS.name
            )
          )
        }

        var allItems = (eventos.map { CalendarItem.Evento(it) } +
               tareas.filter { it.fechaLimite != null }.map { CalendarItem.Tarea(it) } +
               recordatorios.map { CalendarItem.Recordatorio(it) } +
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
          rawEventos = eventos,
          rawTareas = tareas.filter { it.fechaLimite != null },
          rawRecordatorios = recordatorios
        )
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = CalendarState()
    )

  fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
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

  fun loadMoreHistory() {
    _historyPage.value += 1
  }

  fun updateEvento(evento: EventoEntity) {
    viewModelScope.launch {
      eventoDao.updateEvento(evento)
      if (evento.fecha > System.currentTimeMillis()) {
        reminderScheduler.scheduleReminder(
          id = (evento.id + 10000).toInt(),
          title = "Evento: ${evento.titulo}",
          message = "Hoy tienes este evento programado",
          timeInMillis = evento.fecha
        )
      }
    }
  }

  fun deleteEvento(evento: EventoEntity) {
    viewModelScope.launch {
      eventoDao.deleteEvento(evento)
      reminderScheduler.cancelReminder((evento.id + 10000).toInt())
    }
  }

  fun importShiftsFromCsv(content: String) {
    viewModelScope.launch {
      try {
        val lines = content.lines()
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val householdId = currentHouseholdProvider.getCurrentHouseholdId()
        lines.forEach { line ->
          val parts = line.split(",")
          if (parts.size >= 2) {
            val dateStr = parts[0].trim()
            val title = parts[1].trim()
            val date = dateFormat.parse(dateStr)?.time
            if (date != null) {
              val id = eventoDao.insertEvento(
                EventoEntity(
                  hogarId = householdId,
                  titulo = "Turno: $title",
                  fecha = date,
                  tipo = TipoEvento.REUNION.name
                )
              )
              
              if (date > System.currentTimeMillis()) {
                reminderScheduler.scheduleReminder(
                  id = (id + 10000).toInt(),
                  title = "Turno hoy: $title",
                  message = "Recuerda tu turno de trabajo para hoy",
                  timeInMillis = date + (8 * 60 * 60 * 1000) // 8 AM
                )
              }
            }
          }
        }
      } catch (e: Exception) {
        // Error log
      }
    }
  }
}

sealed class CalendarItem {
  abstract val timestamp: Long
  abstract val title: String

  data class Evento(val entity: EventoEntity) : CalendarItem() {
    override val timestamp = entity.fecha
    override val title = entity.titulo
  }
  data class Tarea(val entity: TareaEntity) : CalendarItem() {
    override val timestamp = entity.fechaLimite ?: 0L
    override val title = entity.titulo
  }
  data class Recordatorio(val entity: RecordatorioEntity) : CalendarItem() {
    override val timestamp = entity.fechaHora
    override val title = entity.titulo
  }
}

data class CalendarState(
  val upcoming: List<CalendarItem> = emptyList(),
  val history: List<CalendarItem> = emptyList(),
  val hasArchive: Boolean = false,
  val rawEventos: List<EventoEntity> = emptyList(),
  val rawTareas: List<TareaEntity> = emptyList(),
  val rawRecordatorios: List<RecordatorioEntity> = emptyList()
)
