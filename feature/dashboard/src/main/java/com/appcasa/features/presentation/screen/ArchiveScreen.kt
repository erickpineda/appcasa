package com.appcasa.features.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.AppCasaSutilToast
import com.appcasa.features.finance.data.local.ExpenseEntity
import com.appcasa.features.finance.presentation.viewmodel.FinanceViewModel
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.lists.presentation.viewmodel.ListsViewModel
import com.appcasa.features.maintenance.data.local.MaintenanceEntity
import com.appcasa.features.presentation.viewmodel.HomeMaintenanceViewModel
import com.appcasa.features.tasks.data.local.TareaEntity
import com.appcasa.features.tasks.presentation.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val tabs = listOf("Tareas", "Listas", "Gastos", "Mantenimiento")
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showEmptyVaultDialog by remember { mutableStateOf(false) }
    
    var itemToDelete by remember { mutableStateOf<Any?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

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
        show = showDeleteAllDialog,
        title = "Borrar sección",
        text = "¿Estás seguro de que quieres borrar TODOS los elementos de esta pestaña del archivo? No se podrán recuperar.",
        onConfirm = {
            when(selectedTab) {
                0 -> tasksViewModel.clearAllArchived()
                1 -> listsViewModel.clearAllArchived()
                2 -> financeViewModel.clearAllArchived()
                3 -> maintenanceViewModel.clearAllArchived()
            }
            showDeleteAllDialog = false
        },
        onDismiss = { showDeleteAllDialog = false }
    )

    AppCasaConfirmDialog(
        show = showEmptyVaultDialog,
        title = "Vaciar Caja Fuerte",
        text = "¿Quieres vaciar COMPLETAMENTE el archivo de todas las secciones? Esta acción es irreversible.",
        onConfirm = {
            tasksViewModel.clearAllArchived()
            listsViewModel.clearAllArchived()
            financeViewModel.clearAllArchived()
            maintenanceViewModel.clearAllArchived()
            showEmptyVaultDialog = false
        },
        onDismiss = { showEmptyVaultDialog = false }
    )

    AppCasaConfirmDialog(
        show = itemToDelete != null,
        title = "Eliminar permanentemente",
        text = "¿Estás seguro de que quieres eliminar este elemento para siempre del historial?",
        onConfirm = {
            when(itemToDelete) {
                is TareaEntity -> tasksViewModel.deleteTask(itemToDelete as TareaEntity)
                is ListaEntity -> listsViewModel.deleteList(itemToDelete as ListaEntity)
                is ExpenseEntity -> financeViewModel.deleteExpense(itemToDelete as ExpenseEntity)
                is MaintenanceEntity -> maintenanceViewModel.deleteEvent(itemToDelete as MaintenanceEntity)
            }
            itemToDelete = null
        },
        onDismiss = { itemToDelete = null }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cajón de Archivo") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            when(selectedTab) {
                                0 -> tasksViewModel.archiveOldTasks()
                                2 -> financeViewModel.archiveOldExpenses()
                                3 -> maintenanceViewModel.archiveOldEvents()
                            }
                        }) {
                            Icon(Icons.Default.AutoDelete, contentDescription = "Auto-archivar")
                        }
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Borrar sección")
                        }
                        IconButton(onClick = { showEmptyVaultDialog = true }) {
                            Icon(Icons.Default.Dangerous, contentDescription = "Vaciar todo", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> ArchivedTasksList(tasksViewModel) { itemToDelete = it }
                    1 -> ArchivedListsList(listsViewModel) { itemToDelete = it }
                    2 -> ArchivedExpensesList(financeViewModel) { itemToDelete = it }
                    3 -> ArchivedMaintenanceList(maintenanceViewModel) { itemToDelete = it }
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
fun ArchivedTasksList(viewModel: TasksViewModel, onDelete: (TareaEntity) -> Unit) {
    val tasks by viewModel.archivedTasks.collectAsState()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (tasks.isEmpty()) {
            item { Text("No hay tareas archivadas", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
        } else {
            items(tasks) { task ->
                AppCasaCard(useGlassmorphism = true) {
                    ListItem(
                        headlineContent = { Text(task.titulo) },
                        supportingContent = { Text("Finalizada el ${SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(task.completadoEn ?: 0L))}") },
                        leadingContent = { Icon(Icons.Default.History, null) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { viewModel.unarchiveTask(task.id) }) {
                                    Icon(Icons.Default.Unarchive, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onDelete(task) }) {
                                    Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                }
            }
            item {
                TextButton(
                    onClick = { viewModel.loadMoreArchived() }, 
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cargar más...")
                }
            }
        }
    }
}

@Composable
fun ArchivedListsList(viewModel: ListsViewModel, onDelete: (ListaEntity) -> Unit) {
    val lists by viewModel.archivedLists.collectAsState()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (lists.isEmpty()) {
            item { Text("No hay listas archivadas", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
        } else {
            items(lists) { list ->
                AppCasaCard(useGlassmorphism = true) {
                    ListItem(
                        headlineContent = { Text(list.nombre) },
                        supportingContent = { Text("Tipo: ${list.tipo}") },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.List, null) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { viewModel.unarchiveList(list.id) }) {
                                    Icon(Icons.Default.Unarchive, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onDelete(list) }) {
                                    Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                }
            }
            item {
                TextButton(
                    onClick = { viewModel.loadMoreArchived() }, 
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cargar más...")
                }
            }
        }
    }
}

@Composable
fun ArchivedExpensesList(viewModel: FinanceViewModel, onDelete: (ExpenseEntity) -> Unit) {
    val expenses by viewModel.archivedExpenses.collectAsState()
    Column {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.purgeOldPhotos() }, modifier = Modifier.weight(1f)) {
                Text("Purgar Fotos > 1 año", style = MaterialTheme.typography.labelSmall)
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (expenses.isEmpty()) {
                item { Text("No hay gastos archivados", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
            } else {
                items(expenses) { expense ->
                    AppCasaCard(useGlassmorphism = true) {
                        ListItem(
                            headlineContent = { Text(expense.concepto) },
                            supportingContent = { Text("${String.format(Locale.getDefault(), "%.2f", expense.importe)} € · ${SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(expense.fecha))}") },
                            leadingContent = { Icon(Icons.Default.Payments, null) },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { viewModel.unarchiveExpense(expense.id) }) {
                                        Icon(Icons.Default.Unarchive, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { onDelete(expense) }) {
                                        Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                    }
                }
                item {
                    TextButton(
                        onClick = { viewModel.loadMoreArchived() }, 
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cargar más...")
                    }
                }
            }
        }
    }
}

@Composable
fun ArchivedMaintenanceList(viewModel: HomeMaintenanceViewModel, onDelete: (MaintenanceEntity) -> Unit) {
    val events by viewModel.archivedEvents.collectAsState()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (events.isEmpty()) {
            item { Text("No hay registros de mantenimiento archivados", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
        } else {
            items(events) { event ->
                AppCasaCard(useGlassmorphism = true) {
                    ListItem(
                        headlineContent = { Text(event.titulo) },
                        supportingContent = { Text("${event.categoria} · ${SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(event.fechaRealizacion))}") },
                        leadingContent = { Icon(Icons.Default.Build, null) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { viewModel.unarchiveEvent(event.id) }) {
                                    Icon(Icons.Default.Unarchive, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onDelete(event) }) {
                                    Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                }
            }
            item {
                TextButton(
                    onClick = { viewModel.loadMoreArchived() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cargar más...")
                }
            }
        }
    }
}
