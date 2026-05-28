package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AddTaskViewModel @Inject constructor(
  private val tareaDao: TareaDao,
  private val miembroDao: MiembroDao,
  private val configuracionDao: ConfiguracionDao,
  private val reminderScheduler: ReminderScheduler
) : ViewModel() {

  val familyMembers: StateFlow<List<MiembroEntity>> = miembroDao.getMiembrosByHogar(1L)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  fun addTask(
    titulo: String, 
    prioridad: Prioridad, 
    asignadoId: Long? = null, 
    esPersonal: Boolean = false, 
    fotoUri: String? = null,
    fechaLimite: Long? = null,
    anticipacionMins: Int = 0
  ) {
    viewModelScope.launch {
      val hogarId = configuracionDao.getHogarActual().first()?.id ?: 1L
      val tareaId = tareaDao.insertTarea(
        TareaEntity(
          hogarId = hogarId,
          titulo = titulo,
          prioridad = prioridad.name,
          esPersonal = esPersonal,
          fotoUri = fotoUri,
          fechaLimite = fechaLimite
        )
      )
      
      if (asignadoId != null) {
        tareaDao.insertAsignacion(
          com.appcasa.features.tasks.data.local.TareaAsignacionEntity(
            tareaId = tareaId,
            miembroId = asignadoId
          )
        )
      }

      // Programar notificación con anticipación (Offset de 20000 para tareas)
      fechaLimite?.let { deadline ->
        val scheduledTime = deadline - (anticipacionMins * 60 * 1000)
        if (scheduledTime > System.currentTimeMillis()) {
          reminderScheduler.scheduleReminder(
            id = (tareaId + 20000).toInt(),
            title = "Tarea próxima: $titulo",
            message = if (anticipacionMins > 0) "Aviso: En $anticipacionMins minutos vence tu tarea" else "Tienes una tarea que vence hoy",
            timeInMillis = scheduledTime
          )
        }
      }
    }
  }
}
