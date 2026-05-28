package com.appcasa.features.calendar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.local.EventoEntity
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import com.appcasa.features.reminders.data.local.RecordatorioDao
import com.appcasa.features.reminders.data.local.RecordatorioEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventoDao: EventoDao,
    private val tareaDao: TareaDao,
    private val recordatorioDao: RecordatorioDao
) : ViewModel() {

    private val _historyPage = MutableStateFlow(0)
    val historyPage = _historyPage.asStateFlow()

    // Combinamos eventos, tareas con fecha límite y recordatorios para la vista de calendario
    val calendarItems: StateFlow<CalendarState> = combine(
        eventoDao.getEventosByHogar(1L),
        tareaDao.getTareasByHogar(1L),
        recordatorioDao.getRecordatoriosByHogar(1L)
    ) { eventos, tareas, recordatorios ->
        val now = System.currentTimeMillis()
        
        // Unificamos todos los ítems en una lista plana para procesar
        val allItems = (eventos.map { CalendarItem.Evento(it) } +
                       tareas.filter { it.fechaLimite != null }.map { CalendarItem.Tarea(it) } +
                       recordatorios.map { CalendarItem.Recordatorio(it) })
                       .sortedBy { it.timestamp }

        CalendarState(
            upcoming = allItems.filter { it.timestamp >= now },
            history = allItems.filter { it.timestamp < now }.reversed(), // El historial se ve del más reciente al más antiguo
            rawEventos = eventos,
            rawTareas = tareas.filter { it.fechaLimite != null },
            rawRecordatorios = recordatorios
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarState()
    )

    fun loadMoreHistory() {
        _historyPage.value += 1
    }

    fun importShiftsFromCsv(content: String) {
        viewModelScope.launch {
            try {
                val lines = content.lines()
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                lines.forEach { line ->
                    val parts = line.split(",")
                    if (parts.size >= 2) {
                        val dateStr = parts[0].trim()
                        val title = parts[1].trim()
                        val date = dateFormat.parse(dateStr)?.time
                        if (date != null) {
                            eventoDao.insertEvento(
                                EventoEntity(
                                    hogarId = 1L,
                                    titulo = "Turno: $title",
                                    fecha = date,
                                    tipo = com.appcasa.core.domain.model.TipoEvento.REUNION.name
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
    val rawEventos: List<EventoEntity> = emptyList(),
    val rawTareas: List<TareaEntity> = emptyList(),
    val rawRecordatorios: List<RecordatorioEntity> = emptyList()
)
