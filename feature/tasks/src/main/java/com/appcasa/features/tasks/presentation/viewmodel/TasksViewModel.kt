package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.tasks.GetActiveTasksUseCase
import com.appcasa.core.domain.usecase.config.IsCompactViewUseCase
import com.appcasa.core.domain.usecase.household.GetFamilyMembersUseCase
import com.appcasa.features.tasks.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
  private val getActiveTasksUseCase: GetActiveTasksUseCase,
  private val getArchivedTasksUseCase: GetArchivedTasksUseCase,
  private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
  private val deleteTaskUseCase: DeleteTaskUseCase,
  private val archiveTaskUseCase: ArchiveTaskUseCase,
  private val unarchiveTaskUseCase: UnarchiveTaskUseCase,
  private val clearAllArchivedTasksUseCase: ClearAllArchivedTasksUseCase,
  private val getSubTaskCountsUseCase: GetSubTaskCountsUseCase,
  private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
  private val addTaskUseCase: AddTaskUseCase,
  private val updateTaskUseCase: UpdateTaskUseCase,
  private val archiveOldTasksUseCase: ArchiveOldTasksUseCase,
  private val isCompactViewUseCase: IsCompactViewUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val _showCelebration = MutableStateFlow(false)
  val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

  private val _gainedXP = MutableStateFlow(0)
  val gainedXP: StateFlow<Int> = _gainedXP.asStateFlow()

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  private val _activePage = MutableStateFlow(1)
  val activePage = _activePage.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _toastEvent = MutableSharedFlow<String>(replay = 0)
  val toastEvent = _toastEvent.asSharedFlow()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val tasks: StateFlow<List<Task>> = combine(
    currentHouseholdProvider.householdId,
    _activePage
  ) { id, page -> id to page }
    .flatMapLatest { (id, page) -> 
        getActiveTasksUseCase(id, page)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private val _archivedPage = MutableStateFlow(1)
  val archivedPage = _archivedPage.asStateFlow()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val archivedTasks: StateFlow<List<Task>> = combine(
    currentHouseholdProvider.householdId,
    _archivedPage
  ) { id, page -> id to page }
    .flatMapLatest { (id, page) -> 
        getArchivedTasksUseCase(id, page)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val isCompactView: StateFlow<Boolean> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> isCompactViewUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val subTaskCounts: StateFlow<Map<Long, Pair<Int, Int>>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getSubTaskCountsUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val familyMembers: StateFlow<List<FamilyMember>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getFamilyMembersUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun toggleTaskCompletion(task: Task) {
    viewModelScope.launch {
      val points = toggleTaskCompletionUseCase(task)
      if (points > 0) {
          _gainedXP.value = points
          _showCelebration.value = true
      }
    }
  }

  fun dismissCelebration() {
    _showCelebration.value = false
  }

  fun deleteTask(task: Task) {
    viewModelScope.launch {
      deleteTaskUseCase(task)
    }
  }

  fun archiveTask(task: Task) {
    viewModelScope.launch {
      archiveTaskUseCase(task)
    }
  }

  fun unarchiveTask(taskId: Long) {
    viewModelScope.launch {
      unarchiveTaskUseCase(taskId)
    }
  }

  fun loadMoreActive() {
    if (_isLoading.value) return
    val currentCount = tasks.value.size
    _activePage.value += 1
    viewModelScope.launch {
        _isLoading.value = true
        kotlinx.coroutines.delay(600)
        if (tasks.value.size <= currentCount) {
            _toastEvent.emit("No hay más tareas para cargar")
            _activePage.value -= 1
        }
        _isLoading.value = false
    }
  }

  fun loadMoreArchived() {
    if (_isLoading.value) return
    val currentCount = archivedTasks.value.size
    _archivedPage.value += 1
    viewModelScope.launch {
        _isLoading.value = true
        kotlinx.coroutines.delay(600)
        if (archivedTasks.value.size <= currentCount) {
            _toastEvent.emit("No hay más registros en el archivo")
            _archivedPage.value -= 1
        }
        _isLoading.value = false
    }
  }

  fun clearAllArchived() {
    viewModelScope.launch {
      clearAllArchivedTasksUseCase(householdId)
    }
  }

  fun addTask(titulo: String, prioridad: Prioridad = Prioridad.MEDIA) {
    viewModelScope.launch {
      addTaskUseCase(householdId, titulo, prioridad)
    }
  }

  fun updateTask(task: Task, nuevoTitulo: String) {
    viewModelScope.launch {
      updateTaskUseCase(task, nuevoTitulo)
    }
  }

  fun archiveOldTasks() {
    viewModelScope.launch {
      archiveOldTasksUseCase(householdId)
    }
  }
}
