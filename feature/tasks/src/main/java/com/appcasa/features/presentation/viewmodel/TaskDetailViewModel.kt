package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.tasks.data.local.TareaCheckItemEntity
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val tareaDao: TareaDao,
  private val reminderScheduler: ReminderScheduler
) : ViewModel() {

  private val taskId: Long = checkNotNull(savedStateHandle["taskId"])

  private val _task = MutableStateFlow<TareaEntity?>(null)
  val task: StateFlow<TareaEntity?> = _task.asStateFlow()

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
    fechaLimite: Long?
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
        updatedAt = System.currentTimeMillis()
      )
      tareaDao.updateTarea(updated)
      _task.value = updated
      
      // Reprogramar notificación si tiene fecha
      fechaLimite?.let { deadline ->
        if (deadline > System.currentTimeMillis()) {
          reminderScheduler.scheduleReminder(
            id = (updated.id + 20000).toInt(),
            title = "Tarea próxima: $titulo",
            message = "Tienes una tarea que vence hoy",
            timeInMillis = deadline
          )
        }
      } ?: run {
        reminderScheduler.cancelReminder((updated.id + 20000).toInt())
      }
    }
  }
}
