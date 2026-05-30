package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import com.appcasa.features.tasks.data.local.TareaCheckItemEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
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
      
      val isMarkingAsCompleted = nuevoEstado == EstadoTarea.COMPLETADA.name
      
      val updated = tarea.copy(
        estado = nuevoEstado,
        completadoEn = if (isMarkingAsCompleted) System.currentTimeMillis() else null,
        updatedAt = System.currentTimeMillis()
      )
      tareaDao.updateTarea(updated)
      
      if (isMarkingAsCompleted) {
        reminderScheduler.cancelReminder((tarea.id + 20000).toInt())
        
        // Lógica de Recurrencia
        if (tarea.periodicidad != Periodicidad.NINGUNA.name) {
          spawnNextInstance(tarea)
        }
      }
    }
  }

  private suspend fun spawnNextInstance(tarea: TareaEntity) {
    val nextDate = calculateNextDate(tarea.fechaLimite ?: System.currentTimeMillis(), tarea.periodicidad)
    
    // 1. Clonar Tarea
    val nextTaskId = tareaDao.insertTarea(
      tarea.copy(
        id = 0, // Nueva ID
        estado = EstadoTarea.PENDIENTE.name,
        fechaLimite = nextDate,
        completadoEn = null,
        anticipacionMins = tarea.anticipacionMins,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
    )
    
    // 2. Clonar Sub-tareas
    val subTasks = tareaDao.getCheckItems(tarea.id).first()
    subTasks.forEach { sub ->
      tareaDao.insertCheckItem(
        TareaCheckItemEntity(
          tareaId = nextTaskId,
          texto = sub.texto,
          completado = false,
          orden = sub.orden
        )
      )
    }
  }

  private fun calculateNextDate(currentDate: Long, periodicidad: String): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = currentDate }
    when (periodicidad) {
      Periodicidad.DIARIA.name -> cal.add(Calendar.DAY_OF_YEAR, 1)
      Periodicidad.SEMANAL.name -> cal.add(Calendar.WEEK_OF_YEAR, 1)
      Periodicidad.QUINCENAL.name -> cal.add(Calendar.DAY_OF_YEAR, 15)
      Periodicidad.MENSUAL.name -> cal.add(Calendar.MONTH, 1)
      Periodicidad.TRIMESTRAL.name -> cal.add(Calendar.MONTH, 3)
      Periodicidad.ANUAL.name -> cal.add(Calendar.YEAR, 1)
    }
    return cal.timeInMillis
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
