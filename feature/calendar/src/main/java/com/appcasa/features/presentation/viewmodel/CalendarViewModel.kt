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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventoDao: EventoDao,
    private val tareaDao: TareaDao,
    private val recordatorioDao: RecordatorioDao
) : ViewModel() {

    // Combinamos eventos, tareas con fecha límite y recordatorios para la vista de calendario
    val calendarItems: StateFlow<CalendarState> = combine(
        eventoDao.getEventosByHogar(1L),
        tareaDao.getTareasByHogar(1L),
        recordatorioDao.getRecordatoriosByHogar(1L)
    ) { eventos, tareas, recordatorios ->
        CalendarState(
            eventos = eventos,
            tareasConFecha = tareas.filter { it.fechaLimite != null },
            recordatorios = recordatorios
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarState()
    )

    fun importShiftsFromCsv(content: String) {
        viewModelScope.launch {
            try {
                // Formato esperado: "dd/MM/yyyy, Nombre del Turno"
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
                                    tipo = com.appcasa.core.domain.model.TipoEvento.REUNION.name // Usamos REUNION como base para turnos
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

data class CalendarState(
    val eventos: List<EventoEntity> = emptyList(),
    val tareasConFecha: List<TareaEntity> = emptyList(),
    val recordatorios: List<RecordatorioEntity> = emptyList()
)
