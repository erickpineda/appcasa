package com.appcasa.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.HogarEntity
import com.appcasa.features.settings.data.local.UsuarioEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.local.EventoEntity
import com.appcasa.features.reminders.data.local.RecordatorioDao
import com.appcasa.features.reminders.data.local.RecordatorioEntity
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import com.appcasa.features.utilities.data.local.UtilidadDao
import com.appcasa.features.utilities.data.local.UtilidadEntity
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.inventory.data.local.StockDao
import com.appcasa.features.inventory.data.local.StockEntity
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.finance.data.local.ExpenseEntity
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.dashboard.presentation.model.SearchItem
import com.appcasa.features.dashboard.presentation.model.SearchType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import com.appcasa.navigation.Screen

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
  private val reminderScheduler: ReminderScheduler
) : ViewModel() {

  private val _petCount = MutableStateFlow("0")
  val petCount: StateFlow<String> = _petCount.asStateFlow()

  private val _petSummary = MutableStateFlow("Sin mascotas registradas")
  val petSummary: StateFlow<String> = _petSummary.asStateFlow()

  private val _pendingTasksCount = MutableStateFlow("0")
  val pendingTasksCount: StateFlow<String> = _pendingTasksCount.asStateFlow()

  private val _nextEvent = MutableStateFlow("No hay eventos")
  val nextEvent: StateFlow<String> = _nextEvent.asStateFlow()

  private val _nextEventDate = MutableStateFlow("")
  val nextEventDate: StateFlow<String> = _nextEventDate.asStateFlow()

  private val _monthlyExpense = MutableStateFlow("0.00 €")
  val monthlyExpense: StateFlow<String> = _monthlyExpense.asStateFlow()

  private val _lowStockCount = MutableStateFlow(0)
  val lowStockCount: StateFlow<Int> = _lowStockCount.asStateFlow()

  // Búsqueda Global
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _searchResults = MutableStateFlow<List<SearchItem>>(emptyList())
  val searchResults: StateFlow<List<SearchItem>> = _searchResults.asStateFlow()

  init {
    ensureDefaultHogar()
    observeData()
    setupSearch()
  }

  private fun ensureDefaultHogar() {
    viewModelScope.launch {
      val currentHogar = configuracionDao.getHogarActual().first()
      if (currentHogar == null) {
        configuracionDao.insertHogar(HogarEntity(id = 1L, nombre = "Mi Hogar", descripcion = "Bienvenido a AppCasa"))
        configuracionDao.insertUsuario(UsuarioEntity(hogarId = 1L, nombre = "Usuario Principal", email = "admin@appcasa.com"))
      }
    }
  }

  @OptIn(kotlinx.coroutines.FlowPreview::class)
  private fun setupSearch() {
    viewModelScope.launch {
      _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .collect { query ->
          if (query.isBlank()) {
            _searchResults.value = emptyList()
          } else {
            performSearch(query)
          }
        }
    }
  }

  private suspend fun performSearch(query: String) {
    val tasks = tareaDao.getTareasByHogar(1L).first()
    val lists = listaDao.getListasByHogar(1L).first()
    val members = miembroDao.getMiembrosByHogar(1L).first()
    val stock = stockDao.getAllStock().first()

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

  private fun observeData() {
    viewModelScope.launch {
      miembroDao.getMiembrosByHogar(1L).collect { miembros ->
        val mascotas = miembros.filter { it.tipo != TipoMiembro.PERSONA.name }
        _petCount.value = mascotas.size.toString()
        val summary = mutableListOf<String>()
        val perros = mascotas.count { it.tipo == TipoMiembro.PERRO.name }
        val gatos = mascotas.count { it.tipo == TipoMiembro.GATO.name }
        val tortugas = mascotas.count { it.tipo == TipoMiembro.TORTUGA.name }
        if (perros > 0) summary.add("$perros perro${if (perros > 1) "s" else ""}")
        if (gatos > 0) summary.add("$gatos gato${if (gatos > 1) "s" else ""}")
        if (tortugas > 0) summary.add("$tortugas tortuga${if (tortugas > 1) "s" else ""}")
        _petSummary.value = if (summary.isEmpty()) "Sin mascotas" else summary.joinToString(" · ")
      }
    }

    viewModelScope.launch {
      tareaDao.getTareasByHogar(1L).collect { tareas ->
        val pendientes = tareas.count { it.estado != EstadoTarea.COMPLETADA.name }
        _pendingTasksCount.value = pendientes.toString()
      }
    }

    viewModelScope.launch {
      expenseDao.getTotalMonthlyExpense(getStartOfMonth()).collect { total ->
        _monthlyExpense.value = "${String.format("%.2f", total ?: 0.0)} €"
      }
    }

    viewModelScope.launch {
      stockDao.getLowStockItems().collect { lowItems ->
        _lowStockCount.value = lowItems.size
      }
    }

    viewModelScope.launch {
      combine(
        eventoDao.getEventosByHogar(1L), 
        recordatorioDao.getRecordatoriosByHogar(1L),
        tareaDao.getTareasByHogar(1L)
      ) { eventos, recordatorios, tareas ->
        (eventos.map { it.titulo to it.fecha } + 
         recordatorios.map { it.titulo to it.fechaHora } +
         tareas.filter { it.fechaLimite != null && it.estado != EstadoTarea.COMPLETADA.name }.map { it.titulo to it.fechaLimite!! })
          .filter { it.second >= System.currentTimeMillis() }
          .sortedBy { it.second }
          .firstOrNull()
      }.collect { proximo ->
        if (proximo != null) {
          _nextEvent.value = proximo.first
          _nextEventDate.value = formatDate(proximo.second)
        } else {
          _nextEvent.value = "Sin eventos próximos"
          _nextEventDate.value = ""
        }
      }
    }
  }

  private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val cal = Calendar.getInstance().apply { time = date }
    val format = if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
      "d 'de' MMMM '(Todo el día)'"
    } else {
      "d 'de' MMMM HH:mm"
    }
    val sdf = java.text.SimpleDateFormat(format, java.util.Locale("es", "ES"))
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
  
  fun seedRealData() {
    viewModelScope.launch {
      utilidadDao.deleteAll(); miembroDao.deleteAll(); tareaDao.deleteAll(); tareaDao.deleteAllCategorias()
      eventoDao.deleteAll(); recordatorioDao.deleteAll(); listaDao.deleteAll(); stockDao.deleteAll(); expenseDao.deleteAll()
      val hogarId = 1L
      configuracionDao.insertHogar(HogarEntity(id = hogarId, nombre = "Mi Hogar", descripcion = "AppCasa de la familia"))
      configuracionDao.insertUsuario(UsuarioEntity(hogarId = hogarId, nombre = "Usuario Principal", email = "admin@appcasa.com"))
      miembroDao.insertMiembro(MiembroEntity(hogarId = hogarId, nombre = "Yo", tipo = TipoMiembro.PERSONA.name))
      miembroDao.insertMiembro(MiembroEntity(hogarId = hogarId, nombre = "Mi mujer", tipo = TipoMiembro.PERSONA.name))
      miembroDao.insertMiembro(MiembroEntity(hogarId = hogarId, nombre = "Mi hijo", tipo = TipoMiembro.PERSONA.name, fechaNacimiento = 1435356000000L))
      repeat(2) { miembroDao.insertMiembro(MiembroEntity(hogarId = hogarId, nombre = "Perro ${it+1}", tipo = TipoMiembro.PERRO.name)) }
      repeat(4) { miembroDao.insertMiembro(MiembroEntity(hogarId = hogarId, nombre = "Gato ${it+1}", tipo = TipoMiembro.GATO.name)) }
      miembroDao.insertMiembro(MiembroEntity(hogarId = hogarId, nombre = "Tortuga", tipo = TipoMiembro.TORTUGA.name))
      tareaDao.insertTarea(TareaEntity(hogarId = hogarId, titulo = "Sacar la basura", prioridad = com.appcasa.core.domain.model.Prioridad.ALTA.name))
      tareaDao.insertTarea(TareaEntity(hogarId = hogarId, titulo = "Regar las plantas", prioridad = com.appcasa.core.domain.model.Prioridad.MEDIA.name))
      tareaDao.insertTarea(TareaEntity(hogarId = hogarId, titulo = "Comprar pienso para gatos", prioridad = com.appcasa.core.domain.model.Prioridad.ALTA.name))
      val cal = Calendar.getInstance()
      cal.set(2025, Calendar.JUNE, 27); val birthdayTime = cal.timeInMillis
      eventoDao.insertEvento(EventoEntity(hogarId = hogarId, titulo = "Cumpleaños de mi hijo 🎂", fecha = birthdayTime, tipo = com.appcasa.core.domain.model.TipoEvento.CUMPLEANOS.name))
      reminderScheduler.scheduleReminder(101, "¡Cumpleaños Familiar!", "Hoy cumple años tu hijo 🎂", birthdayTime)
      cal.set(2025, Calendar.JUNE, 15); recordatorioDao.insertRecordatorio(RecordatorioEntity(hogarId = hogarId, titulo = "Vacuna Rabia 💉", fechaHora = cal.timeInMillis))
      cal.set(2025, Calendar.JULY, 10); val itvTime = cal.timeInMillis
      eventoDao.insertEvento(EventoEntity(hogarId = hogarId, titulo = "Pasar ITV Coche 🚗", fecha = itvTime, tipo = com.appcasa.core.domain.model.TipoEvento.ITV.name))
      reminderScheduler.scheduleReminder(102, "Trámites Hogar", "Recuerda pasar la ITV al coche 🚗", itvTime - (7 * 24 * 60 * 60 * 1000))
      val listaCompraId = listaDao.insertLista(ListaEntity(hogarId = hogarId, nombre = "Lista de la Compra", tipo = com.appcasa.core.domain.model.TipoLista.COMPRA.name))
      listaDao.insertItem(com.appcasa.features.lists.data.local.ListaItemEntity(listaId = listaCompraId, texto = "Leche"))
      listaDao.insertItem(com.appcasa.features.lists.data.local.ListaItemEntity(listaId = listaCompraId, texto = "Pienso Perros"))
      stockDao.insertItem(StockEntity(hogarId = hogarId, nombre = "Pienso Gatos", categoria = "Mascotas", cantidadActual = 2.0, cantidadMinima = 1.0, unidad = "Sacos"))
      stockDao.insertItem(StockEntity(hogarId = hogarId, nombre = "Leche", categoria = "Despensa", cantidadActual = 6.0, cantidadMinima = 2.0, unidad = "Litros"))
      stockDao.insertItem(StockEntity(hogarId = hogarId, nombre = "Papel Higiénico", categoria = "Limpieza", cantidadActual = 1.0, cantidadMinima = 4.0, unidad = "Paquetes"))
      expenseDao.insertExpense(ExpenseEntity(hogarId = hogarId, concepto = "Súper Semanal", importe = 85.50, categoria = "Comida"))
      expenseDao.insertExpense(ExpenseEntity(hogarId = hogarId, concepto = "Veterinario", importe = 45.00, categoria = "Mascotas"))
      val initial = listOf(
        UtilidadEntity(codigo = "CALC_DOSIS", nombre = "Dosis Mascotas", descripcion = "Cálculo según peso", icono = "medication", orden = 1, categoria = "Salud"),
        UtilidadEntity(codigo = "CALC_IMC", nombre = "IMC Familiar", descripcion = "Índice de Masa Corporal", icono = "monitor_weight", orden = 2, categoria = "Salud"),
        UtilidadEntity(codigo = "CALC_HIPOTECA", nombre = "Hipoteca", descripcion = "Cuota mensual", icono = "home", orden = 3, categoria = "Finanzas"),
        UtilidadEntity(codigo = "CALC_EDAD", nombre = "Edad Exacta", descripcion = "Años, meses y días", icono = "cake", orden = 4, categoria = "General"),
        UtilidadEntity(codigo = "CALC_CONSUMO", nombre = "Consumo Eléctrico", icono = "bolt", orden = 5, categoria = "Finanzas"),
        UtilidadEntity(codigo = "CALC_AHORRO", nombre = "Ahorro Mensual", icono = "savings", orden = 6, categoria = "Finanzas"),
        UtilidadEntity(codigo = "CALC_UNIDADES", nombre = "Conversor Unidades", descripcion = "Cocina y medidas", icono = "straighten", orden = 7, categoria = "General"),
        UtilidadEntity(codigo = "FIN_GASTOS", nombre = "Diario de Gastos", descripcion = "Control de dinero", icono = "payments", orden = 8, categoria = "Finanzas"),
        UtilidadEntity(codigo = "VEH_MGR", nombre = "Mi Vehículo", descripcion = "Seguro y Matrícula", icono = "directions_car", orden = 9, categoria = "Varios")
      )
      initial.forEach { utilidadDao.insertUtilidad(it) }
    }
  }
}
