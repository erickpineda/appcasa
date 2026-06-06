package com.appcasa.features.presentation.viewmodel

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
import com.appcasa.core.domain.usecase.GetConfigurationUseCase
import com.appcasa.core.domain.usecase.UpdateConfigurationUseCase
import com.appcasa.core.domain.usecase.GetCurrentUserUseCase
import com.appcasa.core.domain.usecase.GetFamilyMembersUseCase
import com.appcasa.core.domain.usecase.GetMemberByIdUseCase
import com.appcasa.core.domain.usecase.UpdateMemberUseCase
import com.appcasa.core.domain.usecase.GetActiveTasksUseCase
import com.appcasa.core.domain.usecase.GetLowStockItemsUseCase
import com.appcasa.core.domain.usecase.GetTotalMonthlyExpenseUseCase
import com.appcasa.features.dashboard.domain.usecase.*
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
  private val getMemberByIdUseCase: GetMemberByIdUseCase,
  private val updateMemberUseCase: UpdateMemberUseCase,
  private val getCurrentUserUseCase: GetCurrentUserUseCase,
  private val getConfigurationUseCase: GetConfigurationUseCase,
  private val updateConfigurationUseCase: UpdateConfigurationUseCase,
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
        config?.ordenModulos?.split(",") ?: listOf(
          Constants.Modules.TASKS,
          Constants.Modules.PETS,
          Constants.Modules.CALENDAR,
          Constants.Modules.EXPENSES,
          Constants.Modules.POSTITS,
        )
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

  val petData: StateFlow<Pair<String, String>> = familyMembers
    .map { miembros ->
        val mascotas = miembros.filter { it.tipo != TipoMiembro.PERSONA }
        val count = mascotas.size.toString()
        val summaryList = mutableListOf<String>()
        val perros = mascotas.count { it.tipo == TipoMiembro.PERRO }
        val gatos = mascotas.count { it.tipo == TipoMiembro.GATO }
        val tortugas = mascotas.count { it.tipo == TipoMiembro.TORTUGA }
        if (perros > 0) summaryList.add("$perros perro${if (perros > 1) "s" else ""}")
        if (gatos > 0) summaryList.add("$gatos gato${if (gatos > 1) "s" else ""}")
        if (tortugas > 0) summaryList.add("$tortugas tortuga${if (tortugas > 1) "s" else ""}")
        val summary = if (summaryList.isEmpty()) "Sin mascotas" else summaryList.joinToString(" · ")
        count to summary
    }
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
        val miembro = getMemberByIdUseCase(miembroId)
        miembro?.let {
            updateMemberUseCase(it.copy(
                estadoAnimo = emoji,
                estadoAnimoUpdatedAt = if (emoji != null) System.currentTimeMillis() else null
            ))
        }
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
