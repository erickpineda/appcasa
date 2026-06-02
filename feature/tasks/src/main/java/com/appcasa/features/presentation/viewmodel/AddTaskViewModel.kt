package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.tasks.data.local.TareaAsignacionEntity
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
  private val tareaDao: TareaDao,
  private val miembroDao: MiembroDao,
  private val configuracionDao: ConfiguracionDao,
  private val reminderScheduler: ReminderScheduler,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  val familyMembers: StateFlow<List<MiembroEntity>> = miembroDao.getMiembrosByHogar(householdId)
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
    anticipacionMins: Int = 0,
    periodicidad: Periodicidad = Periodicidad.NINGUNA,
    tipoContenido: TipoContenidoTarea = TipoContenidoTarea.LISTA
  ) {
    viewModelScope.launch {
      val tareaId = tareaDao.insertTarea(
        TareaEntity(
          hogarId = householdId,
          titulo = titulo,
          prioridad = prioridad.name,
          tipoContenido = tipoContenido.name,
          esPersonal = esPersonal,
          fotoUri = fotoUri,
          fechaLimite = fechaLimite,
          periodicidad = periodicidad.name,
          anticipacionMins = anticipacionMins
        )
      )
      
      if (asignadoId != null) {
        tareaDao.insertAsignacion(
          TareaAsignacionEntity(
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
