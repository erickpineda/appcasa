package com.appcasa.features.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Task
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.core.utils.Constants
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.dashboard.data.local.DashboardConfigEntity
import com.appcasa.features.dashboard.data.local.DashboardDao
import com.appcasa.features.dashboard.data.local.PostItEntity
import com.appcasa.features.dashboard.presentation.model.SearchItem
import com.appcasa.features.dashboard.presentation.model.SearchType
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.inventory.data.local.StockDao
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.maintenance.data.local.MaintenanceDao
import com.appcasa.features.maintenance.data.local.MaintenanceEntity
import com.appcasa.features.reminders.data.local.RecordatorioDao
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.HogarEntity
import com.appcasa.features.settings.data.local.UsuarioEntity
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.utilities.data.local.UtilidadDao
import com.appcasa.features.utilities.data.local.UtilidadEntity
import com.appcasa.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
  private val configuracionDao: ConfiguracionDao,
  private val miembroDao: MiembroDao,
  private val tareaDao: TareaDao,
  private val eventoDao: EventoDao,
  private val recordatorioDao: RecordatorioDao,
  private val utilidadDao: UtilidadDao,
  private val listaDao: ListaDao,
  private val stockDao: StockDao,
  private val expenseDao: ExpenseDao,
  private val dashboardDao: DashboardDao,
  private val maintenanceDao: MaintenanceDao,
  private val reminderScheduler: ReminderScheduler,
  private val currentHouseholdProvider: CurrentHouseholdProvider,
) : ViewModel() {

  private val _postIts = MutableStateFlow<List<PostItEntity>>(emptyList())
  val postIts = _postIts.asStateFlow()

  private val _dashboardOrder = MutableStateFlow(listOf(
    Constants.Modules.TASKS, 
    Constants.Modules.PETS, 
    Constants.Modules.CALENDAR, 
    Constants.Modules.EXPENSES, 
    Constants.Modules.POSTITS
  ))
  val dashboardOrder = _dashboardOrder.asStateFlow()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val familyMembers: StateFlow<List<MiembroEntity>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> miembroDao.getMiembrosByHogar(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val petData: StateFlow<Pair<String, String>> = familyMembers
    .map { miembros ->
        val mascotas = miembros.filter { it.tipo != TipoMiembro.PERSONA.name }
        val count = mascotas.size.toString()
        val summaryList = mutableListOf<String>()
        val perros = mascotas.count { it.tipo == TipoMiembro.PERRO.name }
        val gatos = mascotas.count { it.tipo == TipoMiembro.GATO.name }
        val tortugas = mascotas.count { it.tipo == TipoMiembro.TORTUGA.name }
        if (perros > 0) summaryList.add("$perros perro${if (perros > 1) "s" else ""}")
        if (gatos > 0) summaryList.add("$gatos gato${if (gatos > 1) "s" else ""}")
        if (tortugas > 0) summaryList.add("$tortugas tortuga${if (tortugas > 1) "s" else ""}")
        val summary = if (summaryList.isEmpty()) "Sin mascotas" else summaryList.joinToString(" · ")
        count to summary
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0" to "Sin mascotas registradas")

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val pendingTasksCount: StateFlow<String> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> tareaDao.getTareasByHogar(id) }
    .map { tareas -> tareas.count { it.estado != EstadoTarea.COMPLETADA.name }.toString() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val monthlyExpense: StateFlow<String> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> expenseDao.getTotalMonthlyExpense(id, getStartOfMonth()) }
    .map { total -> String.format(Locale.getDefault(), "%.2f €", total ?: 0.0) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0.00 €")

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val lowStockCount: StateFlow<Int> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> stockDao.getLowStockItems(id) }
    .map { it.size }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val nextEventData: StateFlow<Pair<String, String>> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
        combine(
            eventoDao.getEventosByHogar(id), 
            recordatorioDao.getRecordatoriosByHogar(id),
            tareaDao.getTareasByHogar(id),
            miembroDao.getMiembrosByHogar(id)
        ) { eventos, recordatorios, tareas, miembros ->
            val birthdays = miembros.filter { it.fechaNacimiento != null }.map { 
              "Cumpleaños: ${it.nombre} 🎂" to calculateBirthdayOccurrence(it.fechaNacimiento!!)
            }
            
            (eventos.map { it.titulo to it.fecha } + 
             recordatorios.map { it.titulo to it.fechaHora } +
             tareas.filter { it.fechaLimite != null && it.estado != EstadoTarea.COMPLETADA.name }.map { it.titulo to it.fechaLimite!! } +
             birthdays)
              .filter { it.second >= System.currentTimeMillis() }
              .sortedBy { it.second }
              .firstOrNull()
        }
    }
    .map { proximo ->
        if (proximo != null) {
            proximo.first to formatDate(proximo.second)
        } else {
            "Sin eventos próximos" to ""
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "No hay eventos" to "")

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _searchResults = MutableStateFlow<List<SearchItem>>(emptyList())
  val searchResults: StateFlow<List<SearchItem>> = _searchResults.asStateFlow()

  init {
    viewModelScope.launch {
      currentHouseholdProvider.householdId.collect { id ->
        ensureDefaultHogar(id)
        observeDashboardExtras(id)
      }
    }
    setupSearch()
  }

  private fun observeDashboardExtras(id: Long) {
    viewModelScope.launch {
      dashboardDao.getPostIts(id).collect { _postIts.value = it }
    }
    viewModelScope.launch {
      dashboardDao.getConfig(id).collect { config ->
        config?.ordenModulos?.let {
          _dashboardOrder.value = it.split(",")
        }
      }
    }
  }

  fun updateDashboardOrder(newOrder: List<String>) {
    val id = currentHouseholdProvider.getCurrentHouseholdId()
    _dashboardOrder.value = newOrder
    viewModelScope.launch {
      dashboardDao.saveConfig(DashboardConfigEntity(id, newOrder.joinToString(",")))
    }
  }

  fun addPostIt(contenido: String, color: String = "#FFF9C4") {
    val id = currentHouseholdProvider.getCurrentHouseholdId()
    viewModelScope.launch {
      dashboardDao.insertPostIt(PostItEntity(hogarId = id, contenido = contenido, colorHex = color))
    }
  }

  fun deletePostIt(postIt: PostItEntity) {
    viewModelScope.launch {
      dashboardDao.deletePostIt(postIt)
    }
  }

  fun updateMemberMood(miembroId: Long, emoji: String) {
    viewModelScope.launch {
        val miembro = miembroDao.getMiembroById(miembroId)
        miembro?.let {
            miembroDao.updateMiembro(it.copy(
                estadoAnimo = emoji,
                estadoAnimoUpdatedAt = System.currentTimeMillis()
            ))
        }
    }
  }

  private fun ensureDefaultHogar(id: Long) {
    viewModelScope.launch {
      val members = miembroDao.getMiembrosByHogar(id).first()
      if (members.isEmpty()) {
        seedRealData(id)
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
            performSearch(query, id)
          }
        }
    }
  }

  private suspend fun performSearch(query: String, id: Long) {
    val tasks = tareaDao.getTareasByHogar(id).first()
    val lists = listaDao.getListasByHogar(id).first()
    val members = miembroDao.getMiembrosByHogar(id).first()
    val stock = stockDao.getStockByHogar(id).first()

    val results = mutableListOf<SearchItem>()

    results.addAll(
      tasks.filter { it.titulo.contains(query, ignoreCase = true) }
        .map { SearchItem(it.id, it.titulo, SearchType.TASK, Icons.Default.Task, Screen.TaskDetail.createRoute(it.id)) }
    )
    results.addAll(
      lists.filter { it.nombre.contains(query, ignoreCase = true) }
        .map { SearchItem(it.id, it.nombre, SearchType.LIST, Icons.AutoMirrored.Filled.List, Screen.ListDetail.createRoute(it.id)) }
    )
    results.addAll(
      members.filter { it.nombre.contains(query, ignoreCase = true) }
        .map { 
          val route = if (it.tipo == TipoMiembro.PERSONA.name) Screen.MemberDetail.createRoute(it.id) else Screen.PetDetail.createRoute(it.id)
          SearchItem(it.id, it.nombre, SearchType.MEMBER, if (it.tipo == TipoMiembro.PERSONA.name) Icons.Default.Person else Icons.Default.Pets, route) 
        }
    )
    results.addAll(
      stock.filter { it.nombre.contains(query, ignoreCase = true) }
        .map { SearchItem(it.id, it.nombre, SearchType.STOCK, Icons.Default.Inventory, Screen.Inventory.route) }
    )

    _searchResults.value = results
  }

  fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
  }

  private fun calculateBirthdayOccurrence(birthDateMillis: Long): Long {
    val birthDate = Calendar.getInstance().apply { timeInMillis = birthDateMillis }
    val today = Calendar.getInstance()
    
    val occurrence = Calendar.getInstance().apply {
      set(Calendar.YEAR, today.get(Calendar.YEAR))
      set(Calendar.MONTH, birthDate.get(Calendar.MONTH))
      set(Calendar.DAY_OF_MONTH, birthDate.get(Calendar.DAY_OF_MONTH))
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return occurrence.timeInMillis
  }

  private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val cal = Calendar.getInstance().apply { time = date }
    val format = if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
      "d 'de' MMMM '(Todo el día)'"
    } else {
      Constants.Formatting.DATETIME_FORMAT_ES // Or a specific one for dashboard
      "d 'de' MMMM HH:mm"
    }
    val sdf = SimpleDateFormat(format, Locale("es", "ES"))
    return sdf.format(date)
  }

  private fun getStartOfMonth(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
  }
  
  private fun parseDate(dateStr: String): Long {
    return try {
      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
      System.currentTimeMillis()
    }
  }

  fun seedRealData(id: Long) {
    viewModelScope.launch {
      try {
        utilidadDao.deleteAll()
        miembroDao.deleteAll()
        tareaDao.deleteAll()
        tareaDao.deleteAllCategorias()
        eventoDao.deleteAll()
        recordatorioDao.deleteAll()
        listaDao.deleteAll()
        stockDao.deleteAll()
        expenseDao.deleteAll()
        maintenanceDao.getEventsByHogar(id).first().forEach { maintenanceDao.deleteEvent(it) }
        
        configuracionDao.insertHogar(HogarEntity(id = id, nombre = "Hogar de Erick", descripcion = "Gestión familiar oficial"))
        configuracionDao.insertUsuario(UsuarioEntity(hogarId = id, nombre = "Erick", email = "erick@appcasa.com"))
        
        // Miembros con Cumpleaños Oficiales (Sin crear eventos manuales redundantes)
        val erickBirth = parseDate("25/04/1991")
        val aliciaBirth = parseDate("21/09/1988")
        val brianBirth = parseDate("27/06/2023")

        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Erick", tipo = TipoMiembro.PERSONA.name, fechaNacimiento = erickBirth))
        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Alicia", tipo = TipoMiembro.PERSONA.name, fechaNacimiento = aliciaBirth))
        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Brian", tipo = TipoMiembro.PERSONA.name, fechaNacimiento = brianBirth))
        
        // Mascotas
        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Goofy", tipo = TipoMiembro.PERRO.name, raza = "Beagador"))
        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Daisy", tipo = TipoMiembro.PERRO.name, raza = "Beagle"))
        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Salem", tipo = TipoMiembro.GATO.name, colorPelaje = "Negro de mucho pelo"))
        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Toby", tipo = TipoMiembro.GATO.name, colorPelaje = "Negro con manchita blanca"))
        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Sabrina", tipo = TipoMiembro.GATO.name, colorPelaje = "Blanco with partes negras"))
        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Tom", tipo = TipoMiembro.GATO.name, colorPelaje = "Blanco y negro (gordo)"))
        miembroDao.insertMiembro(MiembroEntity(hogarId = id, nombre = "Super", tipo = TipoMiembro.TORTUGA.name, raza = "Testudo marginata"))

        // Novedades de Utilidades (AÑADIDO UTIL_PIENSO)
        val initialUtils = listOf(
          UtilidadEntity(codigo = "CALC_DOSIS", nombre = "Dosis Mascotas", descripcion = "Cálculo según peso", icono = "medication", orden = 1, categoria = "Salud"),
          UtilidadEntity(codigo = "CALC_IMC", nombre = "IMC Familiar", descripcion = "Índice de Masa Corporal", icono = "monitor_weight", orden = 2, categoria = "Salud"),
          UtilidadEntity(codigo = "CALC_HIPOTECA", nombre = "Hipoteca", descripcion = "Cuota mensual", icono = "home", orden = 3, categoria = "Finanzas"),
          UtilidadEntity(codigo = "FIN_GASTOS", nombre = "Gastos", descripcion = "Control presupuesto", icono = "payments", orden = 4, categoria = "Finanzas"),
          UtilidadEntity(codigo = "VEH_MGR", nombre = "Mi Vehículo", descripcion = "Mantenimiento y seguro", icono = "directions_car", orden = 5, categoria = "Varios"),
          UtilidadEntity(codigo = "CALC_EDAD", nombre = "Edad Exacta", icono = "cake", orden = 6, categoria = "Varios"),
          UtilidadEntity(codigo = "UTIL_PDF", nombre = "Fotos a PDF", descripcion = "Convertir imágenes a PDF", icono = "picture_as_pdf", orden = 7, categoria = "Productividad"),
          UtilidadEntity(codigo = "UTIL_WIFI", nombre = "QR WiFi", descripcion = "Compartir clave WiFi", icono = "qr_code", orden = 8, categoria = "Productividad"),
          UtilidadEntity(codigo = "UTIL_COCINA", nombre = "Cocina", descripcion = "Conversor de medidas", icono = "restaurant", orden = 9, categoria = "Varios"),
          UtilidadEntity(codigo = "UTIL_PIENSO", nombre = "Ración Pienso", descripcion = "Guía de alimentación", icono = "pets", orden = 10, categoria = "Salud")
        )
        initialUtils.forEach { utilidadDao.insertUtilidad(it) }

        listaDao.insertLista(ListaEntity(hogarId = id, nombre = "Lista de la Compra", tipo = com.appcasa.core.domain.model.TipoLista.COMPRA.name))

        // Datos de Mantenimiento
        maintenanceDao.insertEvent(MaintenanceEntity(hogarId = id, titulo = "Revisión Caldera", categoria = "Climatización", fechaRealizacion = System.currentTimeMillis() - 15552000000L, proximaRevision = System.currentTimeMillis() + 15552000000L, coste = 90.0))
        maintenanceDao.insertEvent(MaintenanceEntity(hogarId = id, titulo = "Cambio Filtros Osmosis", categoria = "Fontanería", fechaRealizacion = System.currentTimeMillis() - 5184000000L, proximaRevision = System.currentTimeMillis() + 10368000000L))
        
        reminderScheduler.scheduleReminder(888, "¡Datos Oficiales Cargados!", "Cumpleaños, equipo familiar y plan de mantenimiento sincronizados.", System.currentTimeMillis() + 2000)

      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}
