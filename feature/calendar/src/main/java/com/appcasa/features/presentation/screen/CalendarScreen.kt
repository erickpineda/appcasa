package com.appcasa.features.calendar.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.features.calendar.presentation.viewmodel.CalendarViewModel
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
  val context = LocalContext.current
  var showAddReminderDialog by remember { mutableStateOf(false) }
  var currentMonth by remember { mutableStateOf(YearMonth.now()) }
  var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

  if (showAddReminderDialog) {
    AddReminderDialog(
      onDismiss = { showAddReminderDialog = false },
      onConfirm = { titulo, timeMillis ->
        remindersViewModel.addReminder(titulo, titulo, timeMillis)
        showAddReminderDialog = false
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

  val allItems = (state.eventos.map { "EVENTO" to it } + 
    state.recordatorios.map { "RECORDATORIO" to it } + 
    state.tareasConFecha.map { "TAREA" to it })
    .sortedBy { 
      when(val item = it.second) {
        is com.appcasa.features.calendar.data.local.EventoEntity -> item.fecha
        is com.appcasa.features.reminders.data.local.RecordatorioEntity -> item.fechaHora
        is com.appcasa.features.tasks.data.local.TareaEntity -> item.fechaLimite ?: Long.MAX_VALUE
        else -> Long.MAX_VALUE
      }
    }

  PullToRefreshWrapper {
    CalendarContent(
      allItems = allItems,
      currentMonth = currentMonth,
      selectedDate = selectedDate,
      onMonthChange = { currentMonth = it },
      onEventClick = { dateMillis ->
        val date = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        currentMonth = YearMonth.from(date)
        selectedDate = date
      },
      onImportClick = { filePickerLauncher.launch("text/*") },
      onAddReminderClick = { showAddReminderDialog = true },
      onDeleteReminder = { remindersViewModel.deleteReminder(it) }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContent(
  allItems: List<Pair<String, Any>>,
  currentMonth: YearMonth,
  selectedDate: LocalDate?,
  onMonthChange: (YearMonth) -> Unit,
  onEventClick: (Long) -> Unit,
  onImportClick: () -> Unit,
  onAddReminderClick: () -> Unit = {},
  onDeleteReminder: (com.appcasa.features.reminders.data.local.RecordatorioEntity) -> Unit = {}
) {
  val daysInMonth = currentMonth.lengthOfMonth()
  val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7 // Ajuste Domingo=0

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
            Icon(Icons.Default.UploadFile, contentDescription = "Importar Turnos", tint = MaterialTheme.colorScheme.onPrimary)
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddReminderClick) {
        Icon(Icons.Default.Add, contentDescription = "Nuevo Recordatorio")
      }
    }
  ) { scaffoldPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(scaffoldPadding)
    ) {
      // Calendario Visual (Grid)
      com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true,
        modifier = Modifier.padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
              Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Mes Anterior", modifier = Modifier.size(16.dp))
            }
            Text(
              text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).uppercase()} ${currentMonth.year}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
              Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Mes Siguiente", modifier = Modifier.size(16.dp))
            }
          }
          
          Row(modifier = Modifier.fillMaxWidth()) {
            listOf("D", "L", "M", "X", "J", "V", "S").forEach { day ->
              Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }
          }
          
          LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(200.dp)
          ) {
            // Huecos vacíos antes del día 1
            items(firstDayOfWeek) { Box(modifier = Modifier.aspectRatio(1f)) }
            
            // Días del mes
            items(daysInMonth) { day ->
              val dayNum = day + 1
              val dateAtDay = currentMonth.atDay(dayNum)
              val isToday = dateAtDay == LocalDate.now()
              val isSelected = dateAtDay == selectedDate
              
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
                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSecondary, CircleShape) else Modifier
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = dayNum.toString(),
                  style = MaterialTheme.typography.bodySmall,
                  color = when {
                    isSelected -> MaterialTheme.colorScheme.onSecondary
                    isToday -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                  }
                )
              }
            }
          }
        }
      }

      Text(
        text = "Próximos eventos",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontWeight = FontWeight.Bold
      )

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (allItems.isEmpty()) {
          item {
            Text("No hay eventos próximos", modifier = Modifier.padding(16.dp))
          }
        } else {
          items(allItems) { (type, item) ->
            AgendaItem(
              type = type, 
              item = item,
              onClick = {
                val dateMillis = when(item) {
                    is com.appcasa.features.calendar.data.local.EventoEntity -> item.fecha
                    is com.appcasa.features.reminders.data.local.RecordatorioEntity -> item.fechaHora
                    is com.appcasa.features.tasks.data.local.TareaEntity -> item.fechaLimite ?: 0L
                    else -> 0L
                }
                onEventClick(dateMillis)
              },
              onDelete = {
                if (item is com.appcasa.features.reminders.data.local.RecordatorioEntity) {
                  onDeleteReminder(item)
                }
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun AgendaItem(type: String, item: Any, onClick: () -> Unit, onDelete: () -> Unit = {}) {
  val title: String
  val date: Long
  val icon: androidx.compose.ui.graphics.vector.ImageVector
  val color: Color

  when (item) {
    is com.appcasa.features.calendar.data.local.EventoEntity -> {
      title = item.titulo
      date = item.fecha
      val isBirthday = item.tipo == TipoEvento.CUMPLEANOS.name
      icon = if (isBirthday) Icons.Default.Cake else Icons.Default.Event
      color = if (isBirthday) Color(0xFFFF4081) else MaterialTheme.colorScheme.primary
    }
    is com.appcasa.features.tasks.data.local.TareaEntity -> {
      title = item.titulo
      date = item.fechaLimite ?: 0L
      icon = Icons.Default.Task
      color = MaterialTheme.colorScheme.secondary
    }
    is com.appcasa.features.reminders.data.local.RecordatorioEntity -> {
      title = item.titulo
      date = item.fechaHora
      icon = Icons.Default.Notifications
      color = MaterialTheme.colorScheme.tertiary
    }
    else -> {
      title = "Desconocido"
      date = 0L
      icon = Icons.Default.Event
      color = Color.Gray
    }
  }

  com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth(),
    onClick = onClick
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Icon(icon, contentDescription = null, tint = color)
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = formatDate(date), style = MaterialTheme.typography.bodySmall)
        Text(text = type, style = MaterialTheme.typography.labelSmall, color = color)
      }
      if (item is com.appcasa.features.reminders.data.local.RecordatorioEntity) {
        IconButton(onClick = onDelete) {
          Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
        }
      }
    }
  }
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
          Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDateMillis))}")
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

private fun formatDate(timestamp: Long): String {
  val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
  return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
  AppCasaTheme {
    CalendarContent(
      allItems = listOf(
        "EVENTO" to com.appcasa.features.calendar.data.local.EventoEntity(hogarId = 1, titulo = "Cumpleaños Hijo 🎂", fecha = System.currentTimeMillis() + 86400000, tipo = TipoEvento.CUMPLEANOS.name),
        "RECORDATORIO" to com.appcasa.features.reminders.data.local.RecordatorioEntity(hogarId = 1, titulo = "Vacuna Rabia 💉", fechaHora = System.currentTimeMillis() + 172800000),
        "TAREA" to com.appcasa.features.tasks.data.local.TareaEntity(hogarId = 1, titulo = "Pasar ITV 🚗", fechaLimite = System.currentTimeMillis() + 259200000)
      ),
      currentMonth = YearMonth.now(),
      selectedDate = null,
      onMonthChange = {},
      onEventClick = {},
      onImportClick = {}
    )
  }
}
