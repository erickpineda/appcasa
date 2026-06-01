package com.appcasa.features.calendar.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.core.ui.theme.Birthday
import com.appcasa.feature.calendar.R
import com.appcasa.features.calendar.presentation.viewmodel.CalendarItem
import com.appcasa.features.calendar.presentation.viewmodel.CalendarViewModel
import com.appcasa.features.reminders.presentation.viewmodel.RemindersViewModel
import com.appcasa.navigation.Screen
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
  navController: NavController,
  viewModel: CalendarViewModel = hiltViewModel(),
  remindersViewModel: RemindersViewModel = hiltViewModel()
) {
  val state by viewModel.calendarItems.collectAsState()
  val historyPage by viewModel.historyPage.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
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
          is CalendarItem.Tarea -> { }
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
      navController = navController,
      state = state,
      historyPage = historyPage,
      searchQuery = searchQuery,
      onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
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
      onItemDoubleClick = { item ->
        when (item) {
          is CalendarItem.Tarea -> navController.navigate(Screen.TaskDetail.createRoute(item.entity.id))
          is CalendarItem.Recordatorio -> { editingItem = item }
          is CalendarItem.Evento -> { editingItem = item }
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
  navController: NavController,
  state: com.appcasa.features.calendar.presentation.viewmodel.CalendarState,
  historyPage: Int,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedTab: Int,
  onTabChange: (Int) -> Unit,
  currentMonth: YearMonth,
  selectedDate: LocalDate?,
  selectedItemKey: String?,
  onDateSelected: (LocalDate) -> Unit,
  onItemToggle: (CalendarItem) -> Unit,
  onItemDoubleClick: (CalendarItem) -> Unit,
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
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val listState = androidx.compose.foundation.lazy.rememberLazyListState()

  var isSearching by remember { mutableStateOf(false) }

  // Selector rápido de fecha
  var showJumpDatePicker by remember { mutableStateOf(false) }
  val jumpDatePickerState = rememberDatePickerState()

  if (showJumpDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showJumpDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          jumpDatePickerState.selectedDateMillis?.let {
            val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            onDateSelected(date)
            onMonthChange(YearMonth.from(date))
          }
          showJumpDatePicker = false
        }) { Text(stringResource(R.string.calendar_btn_go_to_date)) }
      },
      dismissButton = {
        TextButton(onClick = { showJumpDatePicker = false }) { Text(stringResource(R.string.calendar_btn_cancel)) }
      }
    ) {
      DatePicker(state = jumpDatePickerState)
    }
  }

  // Efecto profesional: Scroll automático al calendario cuando se selecciona un item
  LaunchedEffect(selectedItemKey) {
    if (selectedItemKey != null) {
      listState.animateScrollToItem(0)
    }
  }

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
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      if (isSearching) {
          TopAppBar(
              title = {
                  TextField(
                      value = searchQuery,
                      onValueChange = onSearchQueryChange,
                      placeholder = { Text("Buscar en la agenda...") },
                      modifier = Modifier.fillMaxWidth(),
                      singleLine = true,
                      colors = TextFieldDefaults.colors(
                          focusedContainerColor = Color.Transparent,
                          unfocusedContainerColor = Color.Transparent,
                          cursorColor = MaterialTheme.colorScheme.onPrimary,
                          focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                          unfocusedTextColor = MaterialTheme.colorScheme.onPrimary
                      ),
                      trailingIcon = {
                          IconButton(onClick = { 
                              onSearchQueryChange("")
                              isSearching = false 
                          }) {
                              Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                          }
                      }
                  )
              },
              colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
          )
      } else {
          MediumTopAppBar(
            title = { Text(stringResource(R.string.calendar_title), fontWeight = FontWeight.Bold) },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.mediumTopAppBarColors(
              containerColor = MaterialTheme.colorScheme.primary,
              scrolledContainerColor = MaterialTheme.colorScheme.primary,
              titleContentColor = MaterialTheme.colorScheme.onPrimary,
              actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
              navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
              }
            },
            actions = {
              IconButton(onClick = { isSearching = true }) {
                  Icon(Icons.Default.Search, contentDescription = "Buscar")
              }
              IconButton(onClick = onImportClick) {
                Icon(Icons.Default.UploadFile, contentDescription = stringResource(R.string.cd_import))
              }
            }
          )
      }
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddReminderClick) {
        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_new))
      }
    }
  ) { scaffoldPadding ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(scaffoldPadding),
      contentPadding = PaddingValues(bottom = 80.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      item {
        AppCasaCard(useGlassmorphism = true,
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
              
              // Título clicable para salto rápido
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(MaterialTheme.shapes.small)
                  .clickable { showJumpDatePicker = true }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).uppercase()} ${currentMonth.year}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Icon(
                  Icons.Default.ArrowDropDown,
                  contentDescription = stringResource(R.string.cd_select_date),
                  modifier = Modifier.size(20.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
              }

              IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp))
              }
            }

            // Botón "Ir a hoy" sutil
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.Center) {
              TextButton(
                onClick = { 
                  val today = LocalDate.now()
                  onDateSelected(today)
                  onMonthChange(YearMonth.from(today))
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
              ) {
                Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.calendar_btn_go_to_today), style = MaterialTheme.typography.labelSmall)
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
              modifier = Modifier.height(280.dp), // Aumentado para visibilidad total de 6 semanas
              userScrollEnabled = false
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
                  Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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
                          .padding(top = 2.dp)
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
      }

      item {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          contentColor = MaterialTheme.colorScheme.primary,
          divider = {}
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { onTabChange(0) },
            text = { Text(stringResource(R.string.calendar_tab_upcoming), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { onTabChange(1) },
            text = { Text(stringResource(R.string.calendar_tab_history), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
          )
        }
      }

      if (selectedTab == 0) {
        val now = LocalDate.now()
        val allUpcoming = state.upcoming
        
        // Vista por día seleccionado
        if (selectedDate != null && selectedItemKey == null) {
          val eventsForDay = (state.upcoming + state.history).filter { 
            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate
          }
          item {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = stringResource(R.string.calendar_events_on_day, SimpleDateFormat("d 'de' MMMM", Locale("es", "ES")).format(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
              )
              Spacer(Modifier.weight(1f))
              TextButton(onClick = { onDateSelected(selectedDate) }) {
                Text(stringResource(R.string.calendar_btn_close), style = MaterialTheme.typography.labelSmall)
              }
            }
          }
          if (eventsForDay.isEmpty()) {
            item { Text(stringResource(R.string.calendar_no_plans_day), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp), color = Color.Gray) }
          } else {
            items(eventsForDay) { item ->
              Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AgendaItemCompact(
                  item = item,
                  isHistory = false,
                  isHighlighted = true,
                  onClick = { onItemToggle(item) },
                  onDoubleClick = { onItemDoubleClick(item) },
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
          item { HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 0.5.dp) }
        }

        val mesActualItems = allUpcoming.filter { 
          val date = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
          date.month == now.month && date.year == now.year
        }
        val otrosMesesItems = allUpcoming.filter { 
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
            item { Text(stringResource(R.string.calendar_no_events_month), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp), color = Color.Gray) }
          } else {
            items(mesActualItems) { item ->
              val itemDate = Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
              val isHighlighted = (selectedItemKey == item.uniqueKey) || (selectedDate == itemDate && selectedItemKey == null)
              
              Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AgendaItemCompact(
                  item = item,
                  isHistory = false,
                  isHighlighted = isHighlighted,
                  onClick = { onItemToggle(item) },
                  onDoubleClick = { onItemDoubleClick(item) },
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
            item { Text(stringResource(R.string.calendar_no_events_future), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp), color = Color.Gray) }
          } else {
            items(otrosMesesItems) { item ->
              val itemDate = Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
              val isHighlighted = (selectedItemKey == item.uniqueKey) || (selectedDate == itemDate && selectedItemKey == null)

              Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AgendaItemCompact(
                  item = item,
                  isHistory = false,
                  isHighlighted = isHighlighted,
                  onClick = { onItemToggle(item) },
                  onDoubleClick = { onItemDoubleClick(item) },
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
        }
      } else {
        val visibleHistory = state.history.take((historyPage + 1) * 10)
        
        if (state.history.isEmpty()) {
          item {
            AppCasaEmptyState(
              title = stringResource(R.string.calendar_empty_history_title),
              description = stringResource(R.string.calendar_empty_history_desc),
              icon = Icons.Default.History,
              modifier = Modifier.fillParentMaxSize()
            )
          }
        } else {
          items(visibleHistory) { item ->
            val itemDate = Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val isHighlighted = (selectedItemKey == item.uniqueKey) || (selectedDate == itemDate && selectedItemKey == null)

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
              AgendaItemCompact(
                item = item,
                isHistory = true,
                isHighlighted = isHighlighted,
                onClick = { onItemToggle(item) },
                onDoubleClick = { onItemDoubleClick(item) },
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
          
          if (state.hasArchive) {
            item {
              AppCasaCard(
                  useGlassmorphism = true,
                  modifier = Modifier.padding(16.dp),
                  onClick = { isSearching = true }
              ) {
                  Row(
                      modifier = Modifier.padding(16.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                      Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                      Column(modifier = Modifier.weight(1f)) {
                          Text("Archivo histórico", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                          Text("Hay registros antiguos ocultos. Usa la búsqueda para encontrarlos.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                      Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                  }
              }
            }
          }

          if (visibleHistory.size < state.history.size) {
            item {
              TextButton(
                onClick = onLoadMoreHistory,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(stringResource(R.string.calendar_load_more_history), style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaItemCompact(
  item: CalendarItem, 
  isHistory: Boolean,
  isHighlighted: Boolean = false,
  onClick: () -> Unit,
  onDoubleClick: () -> Unit,
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
      color = if (isBirthday) Birthday else MaterialTheme.colorScheme.primary
      typeLabel = if (isBirthday) stringResource(R.string.calendar_type_birthday) else stringResource(R.string.calendar_type_event)
    }
    is CalendarItem.Tarea -> {
      icon = Icons.Default.Task
      color = MaterialTheme.colorScheme.secondary
      typeLabel = stringResource(R.string.calendar_type_task)
    }
    is CalendarItem.Recordatorio -> {
      icon = Icons.Default.Notifications
      color = MaterialTheme.colorScheme.tertiary
      typeLabel = stringResource(R.string.calendar_type_reminder)
    }
  }

  AppCasaCard(
    useGlassmorphism = true,
    modifier = Modifier
      .fillMaxWidth()
      .alpha(if (isHistory && !isHighlighted) 0.6f else 1f)
      .combinedClickable(
        onClick = onClick,
        onDoubleClick = onDoubleClick
      ),
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
          val allDaySuffix = stringResource(R.string.calendar_all_day_suffix)
          Text(text = formatDateCompact(item.timestamp, allDaySuffix), style = MaterialTheme.typography.labelSmall)
          Text(text = " • ", style = MaterialTheme.typography.labelSmall)
          Text(text = typeLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
          
          if (item is CalendarItem.Tarea && item.entity.periodicidad != com.appcasa.core.domain.model.Periodicidad.NINGUNA.name) {
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
          }
        }
      }

      if (item !is CalendarItem.Tarea) {
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        }
      }

      IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
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
      .padding(vertical = 4.dp, horizontal = 16.dp),
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

private fun formatDateCompact(timestamp: Long, allDaySuffix: String): String {
  val date = Date(timestamp)
  val cal = Calendar.getInstance().apply { time = date }
  val format = if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
    "d MMM yyyy '$allDaySuffix'"
  } else {
    "d MMM yyyy HH:mm"
  }
  val sdf = SimpleDateFormat(format, Locale("es", "ES"))
  return sdf.format(date)
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
  val timePickerState = rememberTimePickerState()
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
          showDatePicker = false
          showTimePicker = true
        }) { Text(stringResource(R.string.calendar_btn_next_hour)) }
      },
      dismissButton = {
        TextButton(onClick = { 
          selectedDateMillis = datePickerState.selectedDateMillis?.let {
            val cal = Calendar.getInstance().apply { 
              timeInMillis = it 
              set(Calendar.HOUR_OF_DAY, 0)
              set(Calendar.MINUTE, 0)
            }
            cal.timeInMillis
          } ?: System.currentTimeMillis()
          showDatePicker = false
        }) { Text(stringResource(R.string.calendar_btn_all_day)) }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  if (showTimePicker) {
    AlertDialog(
      onDismissRequest = { showTimePicker = false },
      confirmButton = {
        TextButton(onClick = {
          val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
          }
          selectedDateMillis = calendar.timeInMillis
          showTimePicker = false
        }) { Text(stringResource(R.string.calendar_btn_ok)) }
      },
      dismissButton = {
        TextButton(onClick = {
          val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
          }
          selectedDateMillis = calendar.timeInMillis
          showTimePicker = false
        }) { Text(stringResource(R.string.calendar_btn_all_day)) }
      },
      title = { Text(stringResource(R.string.calendar_select_hour)) },
      text = { TimePicker(state = timePickerState) }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.calendar_add_reminder_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = titulo,
          onValueChange = { titulo = it },
          label = { Text(stringResource(R.string.calendar_label_what_to_remember)) },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Button(
          onClick = { showDatePicker = true },
          modifier = Modifier.fillMaxWidth()
        ) {
          val date = Date(selectedDateMillis)
          val cal = Calendar.getInstance().apply { time = date }
          val format = if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
            "dd/MM/yyyy '${stringResource(R.string.calendar_all_day_suffix)}'"
          } else {
            "dd/MM/yyyy HH:mm"
          }
          val sdf = SimpleDateFormat(format, Locale.getDefault())
          Text(stringResource(R.string.calendar_label_date, sdf.format(date)))
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (titulo.isNotBlank()) onConfirm(titulo, selectedDateMillis) }) {
        Text(stringResource(R.string.calendar_btn_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.calendar_btn_cancel)) }
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
  val initialCalendar = Calendar.getInstance().apply { timeInMillis = item.timestamp }
  val timePickerState = rememberTimePickerState(initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY), initialMinute = initialCalendar.get(Calendar.MINUTE))
  
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  var selectedDateMillis by remember { mutableStateOf(item.timestamp) }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDateMillis = datePickerState.selectedDateMillis ?: item.timestamp
          showDatePicker = false
          showTimePicker = true
        }) { Text(stringResource(R.string.calendar_btn_next_hour)) }
      },
      dismissButton = {
        TextButton(onClick = { 
          selectedDateMillis = datePickerState.selectedDateMillis?.let {
            val cal = Calendar.getInstance().apply { 
              timeInMillis = it 
              set(Calendar.HOUR_OF_DAY, 0)
              set(Calendar.MINUTE, 0)
            }
            cal.timeInMillis
          } ?: item.timestamp
          showDatePicker = false
        }) { Text(stringResource(R.string.calendar_btn_all_day)) }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  if (showTimePicker) {
    AlertDialog(
      onDismissRequest = { showTimePicker = false },
      confirmButton = {
        TextButton(onClick = {
          val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
          }
          selectedDateMillis = calendar.timeInMillis
          showTimePicker = false
        }) { Text(stringResource(R.string.calendar_btn_ok)) }
      },
      dismissButton = {
        TextButton(onClick = {
          val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
          }
          selectedDateMillis = calendar.timeInMillis
          showTimePicker = false
        }) { Text(stringResource(R.string.calendar_btn_all_day)) }
      },
      title = { Text(stringResource(R.string.calendar_select_hour)) },
      text = { TimePicker(state = timePickerState) }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(if (item is CalendarItem.Recordatorio) R.string.calendar_edit_reminder_title else R.string.calendar_edit_event_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = titulo,
          onValueChange = { titulo = it },
          label = { Text(stringResource(R.string.calendar_label_title)) },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Button(
          onClick = { showDatePicker = true },
          modifier = Modifier.fillMaxWidth()
        ) {
          val date = Date(selectedDateMillis)
          val cal = Calendar.getInstance().apply { time = date }
          val format = if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
            "dd/MM/yyyy '${stringResource(R.string.calendar_all_day_suffix)}'"
          } else {
            "dd/MM/yyyy HH:mm"
          }
          val sdf = SimpleDateFormat(format, Locale.getDefault())
          Text(stringResource(R.string.calendar_label_datetime, sdf.format(date)))
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (titulo.isNotBlank()) onConfirm(titulo, selectedDateMillis) }) {
        Text(stringResource(R.string.calendar_btn_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.calendar_btn_cancel)) }
    }
  )
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
  AppCasaTheme {
    CalendarContent(
      navController = NavController(LocalContext.current),
      state = com.appcasa.features.calendar.presentation.viewmodel.CalendarState(),
      historyPage = 0,
      searchQuery = "",
      onSearchQueryChange = {},
      selectedTab = 0,
      onTabChange = {},
      currentMonth = YearMonth.now(),
      selectedDate = null,
      selectedItemKey = null,
      onDateSelected = {},
      onItemToggle = {},
      onItemDoubleClick = {},
      onMonthChange = {},
      onEditItem = {},
      onImportClick = {},
      onLoadMoreHistory = {}
    )
  }
}
