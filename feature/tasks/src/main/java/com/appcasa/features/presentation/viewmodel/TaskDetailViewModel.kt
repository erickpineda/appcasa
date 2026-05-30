package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.features.tasks.data.local.TareaCheckItemEntity
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val tareaDao: TareaDao,
  private val miembroDao: MiembroDao,
  private val reminderScheduler: ReminderScheduler
) : ViewModel() {

  private val taskId: Long = checkNotNull(savedStateHandle["taskId"])

  private val _task = MutableStateFlow<TareaEntity?>(null)
  val task: StateFlow<TareaEntity?> = _task.asStateFlow()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val assignedMember: StateFlow<MiembroEntity?> = _task.flatMapLatest { t ->
    if (t == null) flowOf(null)
    else {
        flow<MiembroEntity?> {
            val asignacion = tareaDao.getAsignacionByTarea(t.id)
            if (asignacion == null) emit(null)
            else {
                miembroDao.getMiembrosByHogar(t.hogarId).collect { list ->
                    emit(list.find { it.id == asignacion.miembroId })
                }
            }
        }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val subTasks: StateFlow<List<TareaCheckItemEntity>> = tareaDao.getCheckItems(taskId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  init {
    loadTask()
  }

  private fun loadTask() {
    viewModelScope.launch {
      _task.value = tareaDao.getTareaById(taskId)
    }
  }

  fun addSubTask(texto: String) {
    viewModelScope.launch {
      tareaDao.insertCheckItem(
        TareaCheckItemEntity(
          tareaId = taskId,
          texto = texto,
          orden = subTasks.value.size
        )
      )
    }
  }

  fun toggleSubTask(item: TareaCheckItemEntity) {
    viewModelScope.launch {
      tareaDao.updateCheckItem(item.copy(completado = !item.completado))
    }
  }

  fun updateSubTask(item: TareaCheckItemEntity, nuevoTexto: String) {
    viewModelScope.launch {
      tareaDao.updateCheckItem(item.copy(texto = nuevoTexto))
    }
  }

  fun deleteSubTask(item: TareaCheckItemEntity) {
    viewModelScope.launch {
      tareaDao.deleteCheckItem(item)
    }
  }

  fun deleteSubTasks(itemsToDelete: List<TareaCheckItemEntity>) {
    viewModelScope.launch {
      itemsToDelete.forEach { tareaDao.deleteCheckItem(it) }
    }
  }

  fun toggleSubTasksCompletion(itemsToUpdate: List<TareaCheckItemEntity>, completed: Boolean) {
    viewModelScope.launch {
      itemsToUpdate.forEach { 
        tareaDao.updateCheckItem(it.copy(completado = completed))
      }
    }
  }

  fun updateTask(
    titulo: String, 
    descripcion: String?, 
    prioridad: String, 
    esPersonal: Boolean, 
    fotoUri: String?,
    fechaLimite: Long?,
    anticipacionMins: Int = 0,
    periodicidad: Periodicidad = Periodicidad.NINGUNA,
    tipoContenido: TipoContenidoTarea = TipoContenidoTarea.LISTA
  ) {
    viewModelScope.launch {
      val current = _task.value ?: return@launch
      val updated = current.copy(
        titulo = titulo,
        descripcion = descripcion,
        prioridad = prioridad,
        esPersonal = esPersonal,
        fotoUri = fotoUri,
        fechaLimite = fechaLimite,
        periodicidad = periodicidad.name,
        tipoContenido = tipoContenido.name,
        anticipacionMins = anticipacionMins,
        updatedAt = System.currentTimeMillis()
      )
      tareaDao.updateTarea(updated)
      _task.value = updated
      
      // Reprogramar notificación con anticipación
      fechaLimite?.let { deadline ->
        val scheduledTime = deadline - (anticipacionMins * 60 * 1000)
        if (scheduledTime > System.currentTimeMillis()) {
          reminderScheduler.scheduleReminder(
            id = (updated.id + 20000).toInt(),
            title = "Tarea próxima: $titulo",
            message = if (anticipacionMins > 0) "Aviso: En $anticipacionMins minutos vence tu tarea" else "Tienes una tarea que vence pronto",
            timeInMillis = scheduledTime
          )
        }
      } ?: run {
        reminderScheduler.cancelReminder((updated.id + 20000).toInt())
      }
    }
  }
}
