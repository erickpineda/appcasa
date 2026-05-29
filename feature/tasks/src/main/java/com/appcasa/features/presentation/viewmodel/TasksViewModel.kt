package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
  private val tareaDao: TareaDao,
  private val configuracionDao: ConfiguracionDao,
  private val reminderScheduler: ReminderScheduler,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  val tasks: StateFlow<List<TareaEntity>> = tareaDao.getTareasByHogar(householdId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val isCompactView: StateFlow<Boolean> = configuracionDao.getConfiguracion(householdId)
    .map { list -> list.find { it.clave == "vista_compacta" }?.valor == "true" }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  fun getSubTasksCount(tareaId: Long) = tareaDao.getCheckItems(tareaId).map { items ->
    items.size to items.count { it.completado }
  }

  fun toggleTaskCompletion(tarea: TareaEntity) {
    viewModelScope.launch {
      val nuevoEstado = if (tarea.estado == EstadoTarea.COMPLETADA.name) {
        EstadoTarea.PENDIENTE.name
      } else {
        EstadoTarea.COMPLETADA.name
      }
      val updated = tarea.copy(
        estado = nuevoEstado,
        completadoEn = if (nuevoEstado == EstadoTarea.COMPLETADA.name) System.currentTimeMillis() else null,
        updatedAt = System.currentTimeMillis()
      )
      tareaDao.updateTarea(updated)
      
      // Si se completa, cancelamos cualquier notificación pendiente de esta tarea
      if (nuevoEstado == EstadoTarea.COMPLETADA.name) {
        reminderScheduler.cancelReminder((tarea.id + 20000).toInt()) // Offset para tareas
      }
    }
  }

  fun deleteTask(tarea: TareaEntity) {
    viewModelScope.launch {
      tareaDao.deleteTarea(tarea)
      reminderScheduler.cancelReminder((tarea.id + 20000).toInt())
    }
  }

  fun addTask(titulo: String, prioridad: String = "MEDIA") {
    viewModelScope.launch {
      tareaDao.insertTarea(
        TareaEntity(
          hogarId = householdId,
          titulo = titulo,
          prioridad = prioridad
        )
      )
    }
  }

  fun updateTask(tarea: TareaEntity, nuevoTitulo: String) {
    viewModelScope.launch {
      tareaDao.insertTarea(tarea.copy(titulo = nuevoTitulo))
    }
  }
}
