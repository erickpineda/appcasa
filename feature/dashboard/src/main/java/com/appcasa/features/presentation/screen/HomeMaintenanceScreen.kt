package com.appcasa.features.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.MaintenanceEvent
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.AppCasaSutilToast
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.feature.dashboard.R
import com.appcasa.features.presentation.viewmodel.HomeMaintenanceViewModel
import com.appcasa.navigation.Screen
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMaintenanceScreen(
    navController: NavController,
    viewModel: HomeMaintenanceViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<MaintenanceEvent?>(null) }
    val context = LocalContext.current
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            toastMessage = message
        }
    }

    val qrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val image = InputImage.fromFilePath(context, it)
            val scanner = BarcodeScanning.getClient()
            scanner.process(image).addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { code ->
                    if (code.startsWith("maintenance/")) {
                        val id = code.substringAfter("maintenance/").toLongOrNull()
                        id?.let { navController.navigate(Screen.MaintenanceDetail.createRoute(it)) }
                    }
                }
            }
        }
    }

    AppCasaConfirmDialog(
        show = eventToDelete != null,
        title = stringResource(R.string.maintenance_delete_title),
        text = stringResource(R.string.maintenance_delete_confirm, eventToDelete?.titulo ?: ""),
        onConfirm = {
            eventToDelete?.let { viewModel.archiveEvent(it) }
            eventToDelete = null
        },
        onDismiss = { eventToDelete = null }
    )

    if (showAddDialog) {
        MaintenanceActionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, cat, desc, date, nextDate, cost ->
                viewModel.addEvent(title, cat, desc, date, nextDate, cost)
                showAddDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshWrapper {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.maintenance_title)) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        actions = {
                            IconButton(onClick = { qrLauncher.launch("image/*") }) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.maintenance_new_title))
                    }
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .imePadding(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (events.isEmpty()) {
                        item {
                            AppCasaEmptyState(
                                title = stringResource(R.string.maintenance_empty_title),
                                description = stringResource(R.string.maintenance_empty_desc),
                                icon = Icons.Default.Build,
                                actionText = stringResource(R.string.maintenance_btn_add_first),
                                onActionClick = { showAddDialog = true },
                                modifier = Modifier.fillParentMaxSize()
                            )
                        }
                    }
                    else {
                        items(events) { event ->
                            MaintenanceCard(
                                event = event,
                                onDelete = { eventToDelete = event },
                                onClick = { navController.navigate(Screen.MaintenanceDetail.createRoute(event.id)) }
                            )
                        }
    
                        item {
                            TextButton(
                                onClick = { viewModel.loadMoreActive() }, 
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.maintenance_load_more))
                            }
                        }
                    }
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
fun MaintenanceCard(
    event: MaintenanceEvent,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    AppCasaCard(useGlassmorphism = true, onClick = onClick) {
        ListItem(
            headlineContent = { Text(event.titulo, fontWeight = FontWeight.Bold) },
            supportingContent = {
                Column {
                    Text(event.categoria, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.maintenance_label_done, formatDate(event.fechaRealizacion)), style = MaterialTheme.typography.bodySmall)
                    event.proximaRevision?.let {
                        Text(stringResource(R.string.maintenance_label_next, formatDate(it)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    if (event.coste != null) {
                        Text(stringResource(R.string.maintenance_label_cost, event.coste.toString()), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            leadingContent = {
                val icon = when (event.categoria) {
                    "Electrodomésticos" -> Icons.Default.Kitchen
                    "Fontanería" -> Icons.Default.WaterDrop
                    "Electricidad" -> Icons.Default.ElectricBolt
                    "Pintura" -> Icons.Default.FormatPaint
                    "Climatización" -> Icons.Default.Air
                    else -> Icons.Default.Build
                }
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceActionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, Long, Long?, Double?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Electrodomésticos") }
    var desc by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var nextDateMillis by remember { mutableStateOf<Long?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showNextDatePicker by remember { mutableStateOf(false) }
    
    val categories = listOf("Electrodomésticos", "Fontanería", "Electricidad", "Pintura", "Climatización", "Estructura", "Otros")
    var expanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) { Text(stringResource(R.string.maintenance_btn_ok)) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showNextDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = nextDateMillis ?: (System.currentTimeMillis() + 31536000000L))
        DatePickerDialog(
            onDismissRequest = { showNextDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    nextDateMillis = datePickerState.selectedDateMillis
                    showNextDatePicker = false
                }) { Text(stringResource(R.string.maintenance_btn_ok)) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.maintenance_new_title)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = title, 
                        onValueChange = { title = it }, 
                        label = { Text(stringResource(R.string.maintenance_label_question)) }, 
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                }
                item {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = cat, onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.family_label_category)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { c ->
                                DropdownMenuItem(text = { Text(c) }, onClick = { cat = c; expanded = false })
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = desc, 
                        onValueChange = { desc = it }, 
                        label = { Text(stringResource(R.string.maintenance_label_details_optional)) }, 
                        modifier = Modifier.fillMaxWidth(), 
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                }
                item {
                    OutlinedTextField(
                        value = cost, 
                        onValueChange = { cost = it }, 
                        label = { Text(stringResource(R.string.maintenance_label_cost_optional)) }, 
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.maintenance_label_done, formatDate(dateMillis)))
                        }
                        Button(onClick = { showNextDatePicker = true }, modifier = Modifier.weight(1f)) {
                            Text(if (nextDateMillis == null) stringResource(R.string.maintenance_btn_add_revision) else stringResource(R.string.maintenance_label_next_format, formatDate(nextDateMillis!!)))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    onConfirm(title, cat, desc.takeIf { it.isNotBlank() }, dateMillis, nextDateMillis, cost.toDoubleOrNull())
                }
            }) { Text(stringResource(R.string.dashboard_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) } }
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
}
