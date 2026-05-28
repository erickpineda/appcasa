package com.appcasa.features.calendar.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.features.calendar.presentation.viewmodel.CalendarViewModel
import com.appcasa.features.calendar.presentation.viewmodel.CalendarItem
import com.appcasa.features.reminders.presentation.viewmodel.RemindersViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarScreen(
  navController: NavController,
  viewModel: CalendarViewModel = hiltViewModel(),
  remindersViewModel: RemindersViewModel = hiltViewModel()
) {
  val state by viewModel.calendarItems.collectAsState()
  val historyPage by viewModel.historyPage.collectAsState()
  val context = LocalContext.current
  
  var showAddReminderDialog by remember { mutableStateOf(false) }
  var editingItem by remember { mutableStateOf<CalendarItem?>(null) }
  var currentMonth by remember { mutableStateOf(YearMonth.now()) }
  
  var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
  var selectedItemKey by remember { mutableStateOf<String?>(null) }
  var selectedTab by remember { mutableStateOf(0) }

  if (showAddReminderDialog) {
    AddReminderDialog(
      onDismiss = { showAddReminderDialog = false },
      onConfirm = { titulo, timeMillis ->
        remindersViewModel.addReminder(titulo, titulo, timeMillis)
        showAddReminderDialog = false
      }
    )
  }

  editingItem?.let { item ->
    EditCalendarItemDialog(
      item = item,
      onDismiss = { editingItem = null },
      onConfirm = { nuevoTitulo, nuevaFecha ->
        when (item) {
          is CalendarItem.Evento -> viewModel.updateEvento(item.entity.copy(titulo = nuevoTitulo, fecha = nuevaFecha))
          is CalendarItem.Recordatorio -> remindersViewModel.updateReminder(item.entity.copy(titulo = nuevoTitulo, fechaHora = nuevaFecha))
          is CalendarItem.Tarea -> { /* Tareas se editan en su modulo */ }
        }
        editingItem = null
      }
    )
  }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    uri?.let {
      try {
        val inputStream = context.contentResolver.openInputStream(it)
        val content = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
        if (content != null) {
          viewModel.importShiftsFromCsv(content)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  PullToRefreshWrapper {
    CalendarContent(
      state = state,
      historyPage = historyPage,
      selectedTab = selectedTab,
      onTabChange = { selectedTab = it },
      currentMonth = currentMonth,
      selectedDate = selectedDate,
      selectedItemKey = selectedItemKey,
      onDateSelected = { date ->
        if (selectedDate == date) {
          selectedDate = null
          selectedItemKey = null
        } else {
          selectedDate = date
          selectedItemKey = null
        }
      },
      onItemToggle = { item ->
        val key = item.uniqueKey
        if (selectedItemKey == key) {
          selectedItemKey = null
          selectedDate = null
        } else {
          selectedItemKey = key
          selectedDate = Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
          currentMonth = YearMonth.from(selectedDate)
        }
      },
      onMonthChange = { 
        currentMonth = it
        selectedDate = null
        selectedItemKey = null
      },
      onEditItem = { editingItem = it },
      onImportClick = { filePickerLauncher.launch("text/*") },
      onAddReminderClick = { showAddReminderDialog = true },
      onDeleteReminder = { remindersViewModel.deleteReminder(it) },
      onDeleteEvento = { viewModel.deleteEvento(it) },
      onLoadMoreHistory = { viewModel.loadMoreHistory() }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContent(
  state: com.appcasa.features.calendar.presentation.viewmodel.CalendarState,
  historyPage: Int,
  selectedTab: Int,
  onTabChange: (Int) -> Unit,
  currentMonth: YearMonth,
  selectedDate: LocalDate?,
  selectedItemKey: String?,
  onDateSelected: (LocalDate) -> Unit,
  onItemToggle: (CalendarItem) -> Unit,
  onMonthChange: (YearMonth) -> Unit,
  onEditItem: (CalendarItem) -> Unit,
  onImportClick: () -> Unit,
  onAddReminderClick: () -> Unit = {},
  onDeleteReminder: (com.appcasa.features.reminders.data.local.RecordatorioEntity) -> Unit = {},
  onDeleteEvento: (com.appcasa.features.calendar.data.local.EventoEntity) -> Unit = {},
  onLoadMoreHistory: () -> Unit
) {
  val daysInMonth = currentMonth.lengthOfMonth()
  val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7

  var currentMonthExpanded by remember { mutableStateOf(true) }
  var otherMonthsExpanded by remember { mutableStateOf(false) }

  val daysWithEvents = remember(state, currentMonth) {
    val allItems = state.upcoming + state.history
    allItems.filter { 
      val date = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
      date.year == currentMonth.year && date.month == currentMonth.month
    }.map { 
      Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth 
    }.toSet()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Agenda Familiar") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
          IconButton(onClick = onImportClick) {
            Icon(Icons.Default.UploadFile, contentDescription = "Importar", tint = MaterialTheme.colorScheme.onPrimary)
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddReminderClick) {
        Icon(Icons.Default.Add, contentDescription = "Nuevo")
      }
    }
  ) { scaffoldPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(scaffoldPadding)
    ) {
      com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true,
        modifier = Modifier.padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            Text(
              text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).uppercase()} ${currentMonth.year}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp))
            }
          }
          
          Row(modifier = Modifier.fillMaxWidth()) {
            listOf("D", "L", "M", "X", "J", "V", "S").forEach { day ->
              Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
          
          LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(180.dp)
          ) {
            items(firstDayOfWeek) { Box(modifier = Modifier.aspectRatio(1f)) }
            items(daysInMonth) { day ->
              val dayNum = day + 1
              val dateAtDay = currentMonth.atDay(dayNum)
              val isToday = dateAtDay == LocalDate.now()
              val isSelected = dateAtDay == selectedDate
              val hasEvent = daysWithEvents.contains(dayNum)
              
              Box(
                modifier = Modifier
                  .aspectRatio(1f)
                  .padding(2.dp)
                  .clip(CircleShape)
                  .background(
                    when {
                      isSelected -> MaterialTheme.colorScheme.secondary
                      isToday -> MaterialTheme.colorScheme.primary
                      else -> Color.Transparent
                    }
                  )
                  .then(
                    if (isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.onSecondary, CircleShape) else Modifier
                  )
                  .clickable { onDateSelected(dateAtDay) },
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = dayNum.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                      isSelected -> MaterialTheme.colorScheme.onSecondary
                      isToday -> MaterialTheme.colorScheme.onPrimary
                      else -> MaterialTheme.colorScheme.onSurface
                    }
                  )
                  if (hasEvent) {
                    Box(
                      modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                          if (isSelected || isToday) Color.White else MaterialTheme.colorScheme.primary
                        )
                    )
                  }
                }
              }
            }
          }
        }
      }

      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {}
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { onTabChange(0) },
          text = { Text("PRÓXIMOS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { onTabChange(1) },
          text = { Text("HISTORIAL", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
        )
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (selectedTab == 0) {
          val now = LocalDate.now()
          val mesActualItems = state.upcoming.filter { 
            val date = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            date.month == now.month && date.year == now.year
          }
          val otrosMesesItems = state.upcoming.filter { 
            val date = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            date.isAfter(now.withDayOfMonth(now.lengthOfMonth()))
          }

          item {
            GroupHeader(
              title = "Mes actual",
              count = mesActualItems.size,
              isExpanded = currentMonthExpanded,
              onToggle = { currentMonthExpanded = !currentMonthExpanded }
            )
          }
          
          if (currentMonthExpanded) {
            if (mesActualItems.isEmpty()) {
              item { Text("Sin eventos este mes", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp), color = Color.Gray) }
            } else {
              items(mesActualItems) { item ->
                val itemDate = Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                val isHighlighted = (selectedItemKey == item.uniqueKey) || (selectedDate == itemDate && selectedItemKey == null)
                
                AgendaItemCompact(
                  item = item,
                  isHistory = false,
                  isHighlighted = isHighlighted,
                  onClick = { onItemToggle(item) },
                  onEdit = { onEditItem(item) },
                  onDelete = { 
                    when (item) {
                      is CalendarItem.Recordatorio -> onDeleteReminder(item.entity)
                      is CalendarItem.Evento -> onDeleteEvento(item.entity)
                      is CalendarItem.Tarea -> { }
                    }
                  }
                )
              }
            }
          }

          item { Spacer(modifier = Modifier.height(8.dp)) }

          item {
            GroupHeader(
              title = "Otros meses",
              count = otrosMesesItems.size,
              isExpanded = otherMonthsExpanded,
              onToggle = { otherMonthsExpanded = !otherMonthsExpanded }
            )
          }

          if (otherMonthsExpanded) {
            if (otrosMesesItems.isEmpty()) {
              item { Text("Sin eventos futuros", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp), color = Color.Gray) }
            } else {
              items(otrosMesesItems) { item ->
                val itemDate = Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                val isHighlighted = (selectedItemKey == item.uniqueKey) || (selectedDate == itemDate && selectedItemKey == null)

                AgendaItemCompact(
                  item = item,
                  isHistory = false,
                  isHighlighted = isHighlighted,
                  onClick = { onItemToggle(item) },
                  onEdit = { onEditItem(item) },
                  onDelete = { 
                    when (item) {
                      is CalendarItem.Recordatorio -> onDeleteReminder(item.entity)
                      is CalendarItem.Evento -> onDeleteEvento(item.entity)
                      is CalendarItem.Tarea -> { }
                    }
                  }
                )
              }
            }
          }
        } else {
          val visibleHistory = state.history.take((historyPage + 1) * 10)
          
          if (state.history.isEmpty()) {
            item {
              Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                Text("Historial vacío", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
              }
            }
          } else {
            items(visibleHistory) { item ->
              val itemDate = Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
              val isHighlighted = (selectedItemKey == item.uniqueKey) || (selectedDate == itemDate && selectedItemKey == null)

              AgendaItemCompact(
                item = item,
                isHistory = true,
                isHighlighted = isHighlighted,
                onClick = { onItemToggle(item) },
                onEdit = { onEditItem(item) },
                onDelete = { 
                  when (item) {
                    is CalendarItem.Recordatorio -> onDeleteReminder(item.entity)
                    is CalendarItem.Evento -> onDeleteEvento(item.entity)
                    is CalendarItem.Tarea -> { }
                  }
                }
              )
            }
            
            if (visibleHistory.size < state.history.size) {
              item {
                TextButton(
                  onClick = onLoadMoreHistory,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Ver más eventos pasados...", style = MaterialTheme.typography.labelMedium)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun GroupHeader(title: String, count: Int, isExpanded: Boolean, onToggle: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() }
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(20.dp)
    )
    Text(
      text = title.uppercase(),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.weight(1f)
    )
    Surface(
      color = MaterialTheme.colorScheme.primaryContainer,
      shape = CircleShape,
      modifier = Modifier.size(24.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(count.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun AgendaItemCompact(
  item: CalendarItem, 
  isHistory: Boolean,
  isHighlighted: Boolean = false,
  onClick: () -> Unit,
  onEdit: () -> Unit = {},
  onDelete: () -> Unit = {}
) {
  val icon: androidx.compose.ui.graphics.vector.ImageVector
  val color: Color
  val typeLabel: String

  when (item) {
    is CalendarItem.Evento -> {
      val isBirthday = item.entity.tipo == TipoEvento.CUMPLEANOS.name
      icon = if (isBirthday) Icons.Default.Cake else Icons.Default.Event
      color = if (isBirthday) Color(0xFFFF4081) else MaterialTheme.colorScheme.primary
      typeLabel = if (isBirthday) "Cumpleaños" else "Evento"
    }
    is CalendarItem.Tarea -> {
      icon = Icons.Default.Task
      color = MaterialTheme.colorScheme.secondary
      typeLabel = "Tarea"
    }
    is CalendarItem.Recordatorio -> {
      icon = Icons.Default.Notifications
      color = MaterialTheme.colorScheme.tertiary
      typeLabel = "Recordatorio"
    }
  }

  com.appcasa.core.ui.components.AppCasaCard(
    useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth().alpha(if (isHistory && !isHighlighted) 0.6f else 1f),
    onClick = onClick,
    containerColor = if (isHighlighted) MaterialTheme.colorScheme.surfaceVariant else null
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
      }
      
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.title, 
          style = MaterialTheme.typography.bodyLarge, 
          fontWeight = FontWeight.Bold,
          color = if (isHistory && !isHighlighted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = formatDateCompact(item.timestamp), style = MaterialTheme.typography.labelSmall)
          Text(text = " • ", style = MaterialTheme.typography.labelSmall)
          Text(text = typeLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
      }

      if (item !is CalendarItem.Tarea) {
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        }
      }

      IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
      }
    }
  }
}

private fun formatDateCompact(timestamp: Long): String {
  val sdf = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
  return sdf.format(Date(timestamp))
}

val CalendarItem.uniqueKey: String
  get() = when(this) {
    is CalendarItem.Evento -> "E_${entity.id}"
    is CalendarItem.Tarea -> "T_${entity.id}"
    is CalendarItem.Recordatorio -> "R_${entity.id}"
  }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, Long) -> Unit
) {
  var titulo by remember { mutableStateOf("") }
  val datePickerState = rememberDatePickerState()
  var showDatePicker by remember { mutableStateOf(false) }
  var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
          showDatePicker = false
        }) { Text("OK") }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Nuevo Recordatorio") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = titulo,
          onValueChange = { titulo = it },
          label = { Text("¿Qué recordar?") },
          modifier = Modifier.fillMaxWidth()
        )
        Button(
          onClick = { showDatePicker = true },
          modifier = Modifier.fillMaxWidth()
        ) {
          val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
          Text("Fecha: ${sdf.format(Date(selectedDateMillis))}")
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (titulo.isNotBlank()) onConfirm(titulo, selectedDateMillis) }) {
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCalendarItemDialog(
  item: CalendarItem,
  onDismiss: () -> Unit,
  onConfirm: (String, Long) -> Unit
) {
  var titulo by remember { mutableStateOf(item.title) }
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = item.timestamp)
  var showDatePicker by remember { mutableStateOf(false) }
  var selectedDateMillis by remember { mutableStateOf(item.timestamp) }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDateMillis = datePickerState.selectedDateMillis ?: item.timestamp
          showDatePicker = false
        }) { Text("OK") }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Editar ${if (item is CalendarItem.Recordatorio) "Recordatorio" else "Evento"}") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = titulo,
          onValueChange = { titulo = it },
          label = { Text("Título") },
          modifier = Modifier.fillMaxWidth()
        )
        Button(
          onClick = { showDatePicker = true },
          modifier = Modifier.fillMaxWidth()
        ) {
          val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
          Text("Fecha: ${sdf.format(Date(selectedDateMillis))}")
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (titulo.isNotBlank()) onConfirm(titulo, selectedDateMillis) }) {
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    }
  )
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
  AppCasaTheme {
    CalendarContent(
      state = com.appcasa.features.calendar.presentation.viewmodel.CalendarState(),
      historyPage = 0,
      selectedTab = 0,
      onTabChange = {},
      currentMonth = YearMonth.now(),
      selectedDate = null,
      selectedItemKey = null,
      onDateSelected = {},
      onItemToggle = {},
      onMonthChange = {},
      onEditItem = {},
      onImportClick = {},
      onLoadMoreHistory = {}
    )
  }
}
