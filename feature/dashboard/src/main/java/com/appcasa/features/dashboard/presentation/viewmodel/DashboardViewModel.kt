package com.appcasa.features.dashboard.presentation.viewmodel

import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Task
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.utils.Constants
import com.appcasa.core.domain.repository.DashboardRepository
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.FinanceRepository
import com.appcasa.core.domain.repository.InventoryRepository
import com.appcasa.core.domain.repository.TasksRepository
import com.appcasa.core.domain.usecase.config.GetConfigurationUseCase
import com.appcasa.core.domain.usecase.config.UpdateConfigurationUseCase
import com.appcasa.core.domain.usecase.user.GetCurrentUserUseCase
import com.appcasa.core.domain.usecase.household.GetFamilyMembersUseCase
import com.appcasa.core.domain.usecase.household.UpdateMemberMoodUseCase
import com.appcasa.core.domain.usecase.tasks.GetActiveTasksUseCase
import com.appcasa.core.domain.usecase.inventory.GetLowStockItemsUseCase
import com.appcasa.core.domain.usecase.finance.GetTotalMonthlyExpenseUseCase
import com.appcasa.features.dashboard.domain.usecase.*
import com.appcasa.features.family.domain.usecase.GetPetDataSummaryUseCase
import com.appcasa.features.dashboard.presentation.model.SearchItem
import com.appcasa.features.dashboard.presentation.model.SearchType
import com.appcasa.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
  private val getPostItsUseCase: GetPostItsUseCase,
  private val addPostItUseCase: AddPostItUseCase,
  private val updatePostItUseCase: UpdatePostItUseCase,
  private val deletePostItUseCase: DeletePostItUseCase,
  private val getDashboardConfigUseCase: GetDashboardConfigUseCase,
  private val updateDashboardOrderUseCase: UpdateDashboardOrderUseCase,
  private val searchUseCase: SearchUseCase,
  private val getNextEventUseCase: GetNextEventUseCase,
  private val getActiveTasksUseCase: GetActiveTasksUseCase,
  private val getLowStockItemsUseCase: GetLowStockItemsUseCase,
  private val getTotalMonthlyExpenseUseCase: GetTotalMonthlyExpenseUseCase,
  private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
  private val updateMemberMoodUseCase: UpdateMemberMoodUseCase,
  private val getPetDataSummaryUseCase: GetPetDataSummaryUseCase,
  private val getCurrentUserUseCase: GetCurrentUserUseCase,
  private val getConfigurationUseCase: GetConfigurationUseCase,
  private val updateConfigurationUseCase: UpdateConfigurationUseCase,
  private val tasksRepository: TasksRepository,
  private val financeRepository: FinanceRepository,
  private val familyRepository: FamilyRepository,
  private val inventoryRepository: InventoryRepository,
  private val dashboardRepository: DashboardRepository,
  private val currentHouseholdProvider: CurrentHouseholdProvider,
) : ViewModel() {

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val postIts: StateFlow<List<PostIt>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getPostItsUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val dashboardOrder: StateFlow<List<String>> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
      getDashboardConfigUseCase(id).map { config ->
        val rawOrder = config?.ordenModulos?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        val defaultModules = listOf(
          Constants.Modules.TASKS,
          Constants.Modules.PETS,
          Constants.Modules.CALENDAR,
          Constants.Modules.EXPENSES,
          Constants.Modules.REWARDS,
          Constants.Modules.POSTITS,
        )
        
        val finalOrder = if (rawOrder.isEmpty()) {
            defaultModules
        } else {
            // 1. Identificamos qué módulos "reales" (sin prefijo HIDDEN_) están en la lista guardada
            val existingBaseModules = rawOrder.map { if (it.startsWith("HIDDEN_")) it.substring(7) else it }
            
            // 2. Buscamos módulos nuevos que no estén ni activos ni ocultos (ej. una actualización de la app)
            val newModules = defaultModules.filter { it !in existingBaseModules }
            
            // 3. Devolvemos la lista guardada + los nuevos al final
            rawOrder + newModules
        }

        // Para la UI, solo enviamos los que NO están ocultos
        finalOrder.filter { !it.startsWith("HIDDEN_") }
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val fullDashboardConfig: StateFlow<List<String>> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
        getDashboardConfigUseCase(id).map { config ->
            val rawOrder = config?.ordenModulos?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            val defaultModules = listOf(
              Constants.Modules.TASKS,
              Constants.Modules.PETS,
              Constants.Modules.CALENDAR,
              Constants.Modules.EXPENSES,
              Constants.Modules.REWARDS,
              Constants.Modules.POSTITS,
            )
            if (rawOrder.isEmpty()) defaultModules
            else {
                val existingBase = rawOrder.map { if (it.startsWith("HIDDEN_")) it.substring(7) else it }
                val newOnes = defaultModules.filter { it !in existingBase }
                rawOrder + newOnes
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val quickActions: StateFlow<List<String>> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
      getConfigurationUseCase(id).map { configs ->
        configs.find { it.clave == "DASHBOARD_QUICK_ACTIONS" }?.valor?.split(",")
          ?: listOf("CALC_DOSIS", "UTIL_PDF", "UTIL_SAFE", "LISTS")
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val familyMembers: StateFlow<List<FamilyMember>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getFamilyMembersUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val currentUser: StateFlow<User?> = getCurrentUserUseCase()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val userPoints: StateFlow<Int> = combine(currentUser, familyMembers) { user, members ->
    members.find { it.id == user?.miembroId }?.puntos ?: 0
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val userLevel: StateFlow<Int> = combine(currentUser, familyMembers) { user, members ->
    members.find { it.id == user?.miembroId }?.nivel ?: 1
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

  val petData: StateFlow<Pair<String, String>> = familyMembers
    .map { miembros -> getPetDataSummaryUseCase(miembros) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0" to "Sin mascotas registradas")

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val pendingTasksCount: StateFlow<String> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getActiveTasksUseCase(id, 1) }
    .map { tareas -> tareas.count { it.estado != EstadoTarea.COMPLETADA }.toString() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val monthlyExpense: StateFlow<String> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getTotalMonthlyExpenseUseCase(id) }
    .map { total -> String.format(Locale.getDefault(), "%.2f €", total ?: 0.0) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0.00 €")

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val lowStockCount: StateFlow<Int> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getLowStockItemsUseCase(id) }
    .map { it.size }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val nextEventData: StateFlow<Pair<String, String>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getNextEventUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "No hay eventos" to "")

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _searchResults = MutableStateFlow<List<SearchItem>>(emptyList())
  val searchResults: StateFlow<List<SearchItem>> = _searchResults.asStateFlow()

  private val _isReady = MutableStateFlow(false)
  val isReady = _isReady.asStateFlow()

  init {
    viewModelScope.launch {
      // Sincronizamos la carga inicial: solo mostramos el Dashboard cuando
      // los flujos principales tengan datos reales (al menos una emisión)
      combine(familyMembers, dashboardOrder, quickActions) { _, _, _ -> true }
        .take(1)
        .collect { _isReady.value = true }
    }
    setupSearch()
    
    // Iniciar sincronización remota en tiempo real
    viewModelScope.launch {
        currentHouseholdProvider.householdId.collect { id ->
            tasksRepository.startRemoteSync(id)
            financeRepository.startRemoteSync(id)
            familyRepository.startRemoteSync(id)
            inventoryRepository.startRemoteSync(id)
            dashboardRepository.startRemoteSync(id)
        }
    }
  }

  fun updateDashboardOrder(newOrder: List<String>) {
    val id = currentHouseholdProvider.getCurrentHouseholdId()
    viewModelScope.launch {
      updateDashboardOrderUseCase(id, newOrder)
    }
  }

  fun updateQuickActions(newActions: List<String>) {
      val id = currentHouseholdProvider.getCurrentHouseholdId()
      viewModelScope.launch {
          updateConfigurationUseCase(id, "DASHBOARD_QUICK_ACTIONS", newActions.joinToString(","))
      }
  }

  fun addPostIt(contenido: String, color: String = "#FFF9C4") {
    val id = currentHouseholdProvider.getCurrentHouseholdId()
    viewModelScope.launch {
      addPostItUseCase(id, contenido, color)
    }
  }

  fun updatePostIt(postIt: PostIt, nuevoContenido: String) {
      viewModelScope.launch {
          updatePostItUseCase(postIt.copy(contenido = nuevoContenido))
      }
  }

  fun deletePostIt(postIt: PostIt) {
    viewModelScope.launch {
      deletePostItUseCase(postIt)
    }
  }

  fun updateMemberMood(miembroId: Long, emoji: String?) {
    viewModelScope.launch {
      updateMemberMoodUseCase(miembroId, emoji)
    }
  }

  @OptIn(kotlinx.coroutines.FlowPreview::class)
  private fun setupSearch() {
    viewModelScope.launch {
      combine(_searchQuery, currentHouseholdProvider.householdId) { query, id ->
        query to id
      }
        .debounce(300)
        .distinctUntilChanged()
        .collect { (query, id) ->
          if (query.isBlank()) {
            _searchResults.value = emptyList()
          } else {
            _searchResults.value = searchUseCase(id, query)
          }
        }
    }
  }

  fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
  }

  suspend fun refresh() {
    // Simulamos un breve delay para que el usuario vea el cargador,
    // ya que los datos de Room son muy rápidos
    kotlinx.coroutines.delay(800)
  }
}
