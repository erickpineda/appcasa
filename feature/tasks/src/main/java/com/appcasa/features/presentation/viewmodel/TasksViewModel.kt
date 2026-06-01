package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.tasks.data.local.TareaCheckItemEntity
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
  private val tareaDao: TareaDao,
  private val miembroDao: MiembroDao,
  private val configuracionDao: ConfiguracionDao,
  private val reminderScheduler: ReminderScheduler,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val _showCelebration = MutableStateFlow(false)
  val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

  private val _gainedXP = MutableStateFlow(0)
  val gainedXP: StateFlow<Int> = _gainedXP.asStateFlow()

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

  val subTaskCounts: StateFlow<Map<Long, Pair<Int, Int>>> = tareaDao.getAllCheckItemsCounts(householdId)
    .map { list -> list.associate { it.taskId to (it.total to it.completed) } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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
      
      if (isMarkingAsCompleted && !tarea.puntosOtorgados) {
        reminderScheduler.cancelReminder((tarea.id + 20000).toInt())
        
        // Gamificación: Calcular y otorgar puntos solo si no se dieron antes
        val points = when(tarea.prioridad) {
          Prioridad.ALTA.name -> 20
          Prioridad.BAJA.name -> 5
          else -> 10
        }
        _gainedXP.value = points
        _showCelebration.value = true
        awardPointsForTask(tarea, points)
        
        // Marcamos la tarea para que no vuelva a dar puntos
        tareaDao.updateTarea(updated.copy(puntosOtorgados = true))
      } else {
        tareaDao.updateTarea(updated)
      }
      
      if (isMarkingAsCompleted) {
        // Lógica de Recurrencia (siempre se dispara al completar)
        if (tarea.periodicidad != Periodicidad.NINGUNA.name) {
          spawnNextInstance(tarea)
        }
      }
    }
  }

  private suspend fun awardPointsForTask(tarea: TareaEntity, points: Int) {
    val asignacion = tareaDao.getAsignacionByTarea(tarea.id)
    val memberId = if (asignacion != null) {
      asignacion.miembroId
    } else {
      // Si no hay asignación, buscamos al usuario principal del hogar
      val user = configuracionDao.getUsuarioActual().first()
      // Buscamos un miembro con ese nombre o el primer miembro PERSONA
      miembroDao.getMiembrosByHogar(householdId).first().find { 
        it.nombre == user?.nombre && it.tipo == com.appcasa.core.domain.model.TipoMiembro.PERSONA.name 
      }?.id ?: miembroDao.getMiembrosByHogar(householdId).first().find { 
        it.tipo == com.appcasa.core.domain.model.TipoMiembro.PERSONA.name 
      }?.id
    }

    memberId?.let { id ->
      val miembro = miembroDao.getMiembroById(id)
      miembro?.let { m ->
        val nuevosPuntos = m.puntos + points
        val nuevoNivel = (nuevosPuntos / 100) + 1
        
        miembroDao.updateMiembro(m.copy(
          puntos = nuevosPuntos,
          nivel = nuevoNivel,
          updatedAt = System.currentTimeMillis()
        ))
      }
    }
  }

  fun dismissCelebration() {
    _showCelebration.value = false
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
