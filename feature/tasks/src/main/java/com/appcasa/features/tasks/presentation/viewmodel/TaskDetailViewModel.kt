package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.usecase.household.GetFamilyMembersUseCase
import com.appcasa.features.tasks.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val getTaskByIdUseCase: GetTaskByIdUseCase,
  private val updateTaskUseCase: UpdateTaskUseCase,
  private val getTaskAssignmentsUseCase: GetTaskAssignmentsUseCase,
  private val getTaskCheckItemsUseCase: GetTaskCheckItemsUseCase,
  private val addTaskCheckItemUseCase: AddTaskCheckItemUseCase,
  private val updateTaskCheckItemUseCase: UpdateTaskCheckItemUseCase,
  private val deleteTaskCheckItemUseCase: DeleteTaskCheckItemUseCase,
  private val bulkDeleteTaskCheckItemsUseCase: BulkDeleteTaskCheckItemsUseCase,
  private val bulkUpdateTaskCheckItemsUseCase: BulkUpdateTaskCheckItemsUseCase,
  private val getFamilyMembersUseCase: GetFamilyMembersUseCase
) : ViewModel() {

  private val taskId: Long = checkNotNull(savedStateHandle["taskId"])

  private val _task = MutableStateFlow<Task?>(null)
  val task: StateFlow<Task?> = _task.asStateFlow()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val assignedMember: StateFlow<FamilyMember?> = _task.flatMapLatest { t ->
    if (t == null) flowOf(null)
    else {
        flow<FamilyMember?> {
            val asignaciones = getTaskAssignmentsUseCase(t.id).first()
            val memberId = asignaciones.firstOrNull()?.miembroId
            if (memberId == null) emit(null)
            else {
                getFamilyMembersUseCase(t.hogarId).collect { list ->
                    emit(list.find { it.id == memberId })
                }
            }
        }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val subTasks: StateFlow<List<TaskCheckItem>> = getTaskCheckItemsUseCase(taskId)
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
      _task.value = getTaskByIdUseCase(taskId).first()
    }
  }

  fun addSubTask(texto: String) {
    viewModelScope.launch {
      addTaskCheckItemUseCase(taskId, texto, subTasks.value.size)
    }
  }

  fun toggleSubTask(item: TaskCheckItem) {
    viewModelScope.launch {
      updateTaskCheckItemUseCase(item.copy(completado = !item.completado))
    }
  }

  fun updateSubTask(item: TaskCheckItem, nuevoTexto: String) {
    viewModelScope.launch {
      updateTaskCheckItemUseCase(item.copy(texto = nuevoTexto))
    }
  }

  fun deleteSubTask(item: TaskCheckItem) {
    viewModelScope.launch {
      deleteTaskCheckItemUseCase(item)
    }
  }

  fun deleteSubTasks(itemsToDelete: List<TaskCheckItem>) {
    viewModelScope.launch {
      bulkDeleteTaskCheckItemsUseCase(itemsToDelete)
    }
  }

  fun toggleSubTasksCompletion(itemsToUpdate: List<TaskCheckItem>, completed: Boolean) {
    viewModelScope.launch {
      bulkUpdateTaskCheckItemsUseCase(itemsToUpdate, completed)
    }
  }

  fun updateTask(
    titulo: String, 
    descripcion: String?, 
    prioridad: Prioridad, 
    esPersonal: Boolean, 
    fotoUri: String?,
    fechaLimite: Long?,
    anticipacionMins: Int = 0,
    periodicidad: Periodicidad = Periodicidad.NINGUNA,
    tipoContenido: TipoContenidoTarea = TipoContenidoTarea.LISTA
  ) {
    viewModelScope.launch {
      val current = _task.value ?: return@launch
      updateTaskUseCase(
        task = current,
        nuevoTitulo = titulo,
        nuevaDescripcion = descripcion,
        nuevaPrioridad = prioridad,
        nuevoEsPersonal = esPersonal,
        nuevaFotoUri = fotoUri,
        nuevaFechaLimite = fechaLimite,
        nuevaAnticipacionMins = anticipacionMins,
        nuevaPeriodicidad = periodicidad,
        nuevoTipoContenido = tipoContenido
      )
      // Recargamos el estado local
      _task.value = getTaskByIdUseCase(taskId).first()
    }
  }
}
