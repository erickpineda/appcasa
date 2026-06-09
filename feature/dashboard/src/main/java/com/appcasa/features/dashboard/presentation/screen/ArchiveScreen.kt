package com.appcasa.features.dashboard.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.Expense
import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.MaintenanceEvent
import com.appcasa.core.domain.model.Task
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.AppCasaSutilToast
import com.appcasa.feature.dashboard.R
import com.appcasa.features.finance.presentation.viewmodel.FinanceViewModel
import com.appcasa.features.lists.presentation.viewmodel.ListsViewModel
import com.appcasa.features.dashboard.presentation.viewmodel.HomeMaintenanceViewModel
import com.appcasa.features.tasks.presentation.viewmodel.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
  navController: NavController,
  tasksViewModel: TasksViewModel = hiltViewModel(),
  listsViewModel: ListsViewModel = hiltViewModel(),
  financeViewModel: FinanceViewModel = hiltViewModel(),
  maintenanceViewModel: HomeMaintenanceViewModel = hiltViewModel()
) {
  var selectedTab by remember { mutableStateOf(0) }
  var itemToDelete by remember { mutableStateOf<Any?>(null) }
  var toastMessage by remember { mutableStateOf<String?>(null) }
  var showClearSectionConfirm by remember { mutableStateOf(false) }
  var showClearAllConfirm by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
      tasksViewModel.toastEvent.collect { toastMessage = it }
  }
  LaunchedEffect(Unit) {
      listsViewModel.toastEvent.collect { toastMessage = it }
  }
  LaunchedEffect(Unit) {
      financeViewModel.toastEvent.collect { toastMessage = it }
  }
  LaunchedEffect(Unit) {
      maintenanceViewModel.toastEvent.collect { toastMessage = it }
  }

  AppCasaConfirmDialog(
    show = itemToDelete != null,
    title = stringResource(R.string.archive_delete_title),
    text = stringResource(R.string.archive_delete_confirm),
    onConfirm = {
      when (itemToDelete) {
        is Task -> tasksViewModel.deleteTask(itemToDelete as Task)
        is Lista -> listsViewModel.deleteList(itemToDelete as Lista)
        is Expense -> financeViewModel.deleteExpense(itemToDelete as Expense)
        is MaintenanceEvent -> maintenanceViewModel.deleteEvent(itemToDelete as MaintenanceEvent)
      }
      itemToDelete = null
    },
    onDismiss = { itemToDelete = null }
  )

  if (showClearSectionConfirm) {
      AlertDialog(
          onDismissRequest = { showClearSectionConfirm = false },
          title = { Text(stringResource(R.string.archive_clear_section_title)) },
          text = { Text(stringResource(R.string.archive_clear_section_desc)) },
          confirmButton = {
              Button(onClick = {
                  when(selectedTab) {
                      0 -> tasksViewModel.clearAllArchived()
                      1 -> listsViewModel.clearAllArchived()
                      2 -> financeViewModel.clearAllArchived()
                      3 -> maintenanceViewModel.clearAllArchived()
                  }
                  showClearSectionConfirm = false
              }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                  Text(stringResource(R.string.archive_clear_section_btn))
              }
          },
          dismissButton = { TextButton(onClick = { showClearSectionConfirm = false }) { Text(stringResource(R.string.archive_btn_cancel)) } }
      )
  }

  if (showClearAllConfirm) {
      AlertDialog(
          onDismissRequest = { showClearAllConfirm = false },
          title = { Text(stringResource(R.string.archive_clear_all_title)) },
          text = { Text(stringResource(R.string.archive_clear_all_desc)) },
          confirmButton = {
              Button(onClick = {
                  tasksViewModel.clearAllArchived()
                  listsViewModel.clearAllArchived()
                  financeViewModel.clearAllArchived()
                  maintenanceViewModel.clearAllArchived()
                  showClearAllConfirm = false
              }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                  Text(stringResource(R.string.archive_clear_all_btn))
              }
          },
          dismissButton = { TextButton(onClick = { showClearAllConfirm = false }) { Text(stringResource(R.string.archive_btn_cancel)) } }
      )
  }

  Box(modifier = Modifier.fillMaxSize()) {
      Scaffold(
        topBar = {
          TopAppBar(
            title = { Text(stringResource(R.string.archive_title)) },
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
              }
            },
            actions = {
                IconButton(onClick = { showClearSectionConfirm = true }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.archive_cd_clear_section), tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = { showClearAllConfirm = true }) {
                    Icon(Icons.Default.AutoDelete, contentDescription = stringResource(R.string.archive_cd_clear_all), tint = MaterialTheme.colorScheme.error)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface,
              titleContentColor = MaterialTheme.colorScheme.onSurface,
              navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
          )
        }
      ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
          ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResource(R.string.archive_tab_tasks)) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResource(R.string.archive_tab_lists)) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(stringResource(R.string.archive_tab_finance)) })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text(stringResource(R.string.archive_tab_maintenance)) })
          }
    
          when (selectedTab) {
            0 -> ArchivedTasksList(tasksViewModel, onDelete = { itemToDelete = it })
            1 -> ArchivedListsList(listsViewModel, onDelete = { itemToDelete = it })
            2 -> ArchivedExpensesList(financeViewModel, onDelete = { itemToDelete = it })
            3 -> ArchivedMaintenanceList(maintenanceViewModel, onDelete = { itemToDelete = it })
          }
        }
      }

      AppCasaSutilToast(
          message = toastMessage,
          onDismiss = { toastMessage = null }
      )
  }
}

@Composable
fun ArchivedTasksList(viewModel: TasksViewModel, onDelete: (Task) -> Unit) {
  val tasks by viewModel.archivedTasks.collectAsState()
  if (tasks.isEmpty()) {
    AppCasaEmptyState(title = stringResource(R.string.archive_empty), description = "", icon = Icons.Default.Inventory)
  } else {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(tasks) { task ->
        ArchiveItemCard(title = task.titulo, onRestore = { viewModel.unarchiveTask(task.id) }, onDelete = { onDelete(task) })
      }
      item {
          TextButton(onClick = { viewModel.loadMoreArchived() }, modifier = Modifier.fillMaxWidth()) {
              Text(stringResource(R.string.archive_load_more))
          }
      }
    }
  }
}

@Composable
fun ArchivedListsList(viewModel: ListsViewModel, onDelete: (Lista) -> Unit) {
  val lists by viewModel.archivedLists.collectAsState()
  if (lists.isEmpty()) {
    AppCasaEmptyState(title = stringResource(R.string.archive_empty), description = "", icon = Icons.Default.List)
  } else {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(lists) { lista ->
        ArchiveItemCard(title = lista.nombre, onRestore = { viewModel.unarchiveList(lista.id) }, onDelete = { onDelete(lista) })
      }
      item {
          TextButton(onClick = { viewModel.loadMoreArchived() }, modifier = Modifier.fillMaxWidth()) {
              Text(stringResource(R.string.archive_load_more))
          }
      }
    }
  }
}

@Composable
fun ArchivedExpensesList(viewModel: FinanceViewModel, onDelete: (Expense) -> Unit) {
  val expenses by viewModel.archivedExpenses.collectAsState()
  val currency by viewModel.currencySymbol.collectAsState()
  
  if (expenses.isEmpty()) {
    AppCasaEmptyState(title = stringResource(R.string.archive_empty), description = "", icon = Icons.Default.Payments)
  } else {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(expenses) { expense ->
        ArchiveItemCard(
            title = stringResource(R.string.archive_expense_format, expense.concepto, expense.importe, currency), 
            onRestore = { viewModel.unarchiveExpense(expense.id) }, 
            onDelete = { onDelete(expense) }
        )
      }
      item {
          TextButton(onClick = { viewModel.loadMoreArchived() }, modifier = Modifier.fillMaxWidth()) {
              Text(stringResource(R.string.archive_load_more))
          }
      }
    }
  }
}

@Composable
fun ArchivedMaintenanceList(viewModel: HomeMaintenanceViewModel, onDelete: (MaintenanceEvent) -> Unit) {
  val events by viewModel.archivedEvents.collectAsState()
  if (events.isEmpty()) {
    AppCasaEmptyState(title = stringResource(R.string.archive_empty), description = "", icon = Icons.Default.Build)
  } else {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(events) { event ->
        ArchiveItemCard(title = event.titulo, onRestore = { viewModel.unarchiveEvent(event.id) }, onDelete = { onDelete(event) })
      }
      item {
          TextButton(onClick = { viewModel.loadMoreArchived() }, modifier = Modifier.fillMaxWidth()) {
              Text(stringResource(R.string.archive_load_more))
          }
      }
    }
  }
}

@Composable
fun ArchiveItemCard(title: String, onRestore: () -> Unit, onDelete: () -> Unit) {
  AppCasaCard {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
      IconButton(onClick = onRestore) {
        Icon(Icons.Default.Restore, contentDescription = stringResource(R.string.archive_restore_cd), tint = MaterialTheme.colorScheme.primary)
      }
      IconButton(onClick = onDelete) {
        Icon(Icons.Default.DeleteForever, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error)
      }
    }
  }
}
