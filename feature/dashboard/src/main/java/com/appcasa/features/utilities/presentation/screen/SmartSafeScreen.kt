package com.appcasa.features.utilities.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.Document
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.utils.Constants
import com.appcasa.feature.dashboard.R
import com.appcasa.features.utilities.presentation.viewmodel.SmartSafeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSafeScreen(
  navController: NavController,
  viewModel: SmartSafeViewModel = hiltViewModel()
) {
  val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
  val context = LocalContext.current
  fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
      if (context is FragmentActivity) return context
      context = context.baseContext
    }
    return null
  }

  LaunchedEffect(Unit) {
    if (!isUnlocked) {
      kotlinx.coroutines.delay(300) // Pequeño margen para estabilidad de la UI
      context.findActivity()?.let { viewModel.authenticate(it) }
    }
  }

  if (!isUnlocked) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.util_safe_locked), style = MaterialTheme.typography.titleLarge)
        TextButton(onClick = { 
          context.findActivity()?.let { viewModel.authenticate(it) }
        }) {
          Text(stringResource(R.string.util_safe_unlock_biometric))
        }
      }
    }
    return
  }

  val documentos by viewModel.documentos.collectAsStateWithLifecycle()
  var showAddDialog by remember { mutableStateOf(false) }
  var editingDocument by remember { mutableStateOf<Document?>(null) }
  var documentToDelete by remember { mutableStateOf<Document?>(null) }

  AppCasaConfirmDialog(
    show = documentToDelete != null,
    title = stringResource(R.string.util_safe_delete_title),
    text = stringResource(R.string.util_safe_delete_confirm, documentToDelete?.nombre ?: ""),
    onConfirm = {
      documentToDelete?.let { viewModel.deleteDocumento(it) }
      documentToDelete = null
    },
    onDismiss = { documentToDelete = null }
  )

  if (showAddDialog) {
    AddDocumentDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { nombre, cat, uri, fecha ->
        viewModel.addDocumento(nombre, cat, uri, fecha)
        showAddDialog = false
      }
    )
  }

  if (editingDocument != null) {
    EditDocumentDialog(
      documento = editingDocument!!,
      onDismiss = { editingDocument = null },
      onConfirm = { nuevoNombre, nuevaCat, nuevaFecha ->
        viewModel.updateDocumento(editingDocument!!.copy(
          nombre = nuevoNombre,
          categoria = nuevaCat,
          fechaVencimiento = nuevaFecha
        ))
        editingDocument = null
      }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.util_safe_title)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary,
          navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = { showAddDialog = true }) {
        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.util_safe_btn_add_doc))
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Text(
          stringResource(R.string.util_safe_important_docs_header),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      if (documentos.isEmpty()) {
        item {
          AppCasaEmptyState(
            title = stringResource(R.string.util_safe_empty_title),
            description = stringResource(R.string.util_safe_empty_desc),
            icon = Icons.Default.FolderOpen,
            actionText = stringResource(R.string.util_safe_btn_add_doc),
            onActionClick = { showAddDialog = true },
            modifier = Modifier.fillParentMaxHeight(0.7f)
          )
        }
      } else {
        items(documentos) { doc ->
          DocumentCard(
            documento = doc,
            onOpen = {
              try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                  setDataAndType(Uri.parse(doc.uriPdf), "application/pdf")
                  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.util_safe_open_doc_chooser)))
              } catch (e: Exception) {
                e.printStackTrace()
              }
            },
            onEdit = { editingDocument = doc },
            onDelete = { documentToDelete = doc },
            onCloudSync = { viewModel.uploadToCloud(doc) }
          )
        }
      }
    }
  }
}

@Composable
fun DocumentCard(
  documento: Document,
  onOpen: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onCloudSync: () -> Unit
) {
  AppCasaCard(useGlassmorphism = true) {
    ListItem(
      headlineContent = { Text(documento.nombre, fontWeight = FontWeight.Bold) },
      supportingContent = {
        Column {
          Text(documento.categoria, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
          documento.fechaVencimiento?.let { fecha ->
            val date = SimpleDateFormat(Constants.Formatting.DATE_FORMAT_ES, Locale.getDefault()).format(Date(fecha))
            Text(stringResource(R.string.util_safe_expiry, date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
          }
        }
      },
      leadingContent = {
        val icon = when(documento.categoria) {
          stringResource(R.string.util_safe_cat_education) -> Icons.Default.School
          stringResource(R.string.util_safe_cat_warranty) -> Icons.Default.VerifiedUser
          stringResource(R.string.util_safe_cat_health) -> Icons.Default.LocalHospital
          stringResource(R.string.util_safe_cat_insurance) -> Icons.Default.Shield
          else -> Icons.Default.Description
        }
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      },
      trailingContent = {
        Row {
          IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
          }
          if (!documento.sincronizado) {
            IconButton(onClick = onCloudSync) {
              Icon(Icons.Default.CloudUpload, contentDescription = stringResource(R.string.util_safe_cd_cloud_upload), tint = MaterialTheme.colorScheme.outline)
            }
          } else {
            Icon(Icons.Default.CloudDone, contentDescription = stringResource(R.string.util_safe_cd_synced), tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(12.dp))
          }
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
          }
        }
      },
      modifier = Modifier.clickable { onOpen() }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, Long?) -> Unit
) {
  val warrantyCat = stringResource(R.string.util_safe_cat_warranty)
  val educationCat = stringResource(R.string.util_safe_cat_education)
  val healthCat = stringResource(R.string.util_safe_cat_health)
  val insuranceCat = stringResource(R.string.util_safe_cat_insurance)
  val otherCat = stringResource(R.string.util_safe_cat_others)

  var nombre by remember { mutableStateOf("") }
  var categoria by remember { mutableStateOf(warrantyCat) }
  var selectedUri by remember { mutableStateOf<Uri?>(null) }
  
  var showDatePicker by remember { mutableStateOf(false) }
  val datePickerState = rememberDatePickerState()
  var selectedDate by remember { mutableStateOf<Long?>(null) }
  
  val categories = listOf(warrantyCat, educationCat, healthCat, insuranceCat, otherCat)
  var expanded by remember { mutableStateOf(false) }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    selectedUri = uri
    if (nombre.isBlank() && uri != null) {
      nombre = uri.lastPathSegment?.substringAfterLast("/")?.substringBeforeLast(".") ?: ""
    }
  }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDate = datePickerState.selectedDateMillis
          showDatePicker = false
        }) { Text(stringResource(R.string.family_btn_ok)) }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.util_safe_new_doc_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = nombre, 
          onValueChange = { nombre = it }, 
          label = { Text(stringResource(R.string.util_safe_label_doc_name)) }, 
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
          OutlinedTextField(
            value = categoria,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.util_safe_label_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
          ) {
            categories.forEach { cat ->
              DropdownMenuItem(text = { Text(cat) }, onClick = { categoria = cat; expanded = false })
            }
          }
        }

        OutlinedButton(
          onClick = { filePickerLauncher.launch("application/pdf") },
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(if (selectedUri != null) Icons.Default.CheckCircle else Icons.Default.PictureAsPdf, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text(if (selectedUri != null) stringResource(R.string.util_safe_btn_pdf_selected) else stringResource(R.string.util_safe_btn_choose_pdf))
        }

        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
          val label = if (selectedDate == null) stringResource(R.string.util_safe_label_add_expiry) else stringResource(R.string.util_safe_expiry, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate!!)))
          Text(label)
        }
      }
    },
    confirmButton = {
      Button(onClick = { 
        if (nombre.isNotBlank() && selectedUri != null) {
          onConfirm(nombre, categoria, selectedUri.toString(), selectedDate)
        }
      }) {
        Text(stringResource(R.string.dashboard_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDocumentDialog(
  documento: Document,
  onDismiss: () -> Unit,
  onConfirm: (String, String, Long?) -> Unit
) {
  val warrantyCat = stringResource(R.string.util_safe_cat_warranty)
  val educationCat = stringResource(R.string.util_safe_cat_education)
  val healthCat = stringResource(R.string.util_safe_cat_health)
  val insuranceCat = stringResource(R.string.util_safe_cat_insurance)
  val otherCat = stringResource(R.string.util_safe_cat_others)

  var nombre by remember { mutableStateOf(documento.nombre) }
  var categoria by remember { mutableStateOf(documento.categoria) }
  var showDatePicker by remember { mutableStateOf(false) }
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = documento.fechaVencimiento)
  var selectedDate by remember { mutableStateOf(documento.fechaVencimiento) }
  
  val categories = listOf(warrantyCat, educationCat, healthCat, insuranceCat, otherCat)
  var expanded by remember { mutableStateOf(false) }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDate = datePickerState.selectedDateMillis
          showDatePicker = false
        }) { Text(stringResource(R.string.family_btn_ok)) }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.util_safe_edit_doc_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = nombre, 
          onValueChange = { nombre = it }, 
          label = { Text(stringResource(R.string.family_label_name)) }, 
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
          OutlinedTextField(
            value = categoria,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.util_safe_label_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
          ) {
            categories.forEach { cat ->
              DropdownMenuItem(text = { Text(cat) }, onClick = { categoria = cat; expanded = false })
            }
          }
        }

        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
          val label = if (selectedDate == null) stringResource(R.string.util_safe_label_add_expiry_edit) else stringResource(R.string.util_safe_expiry, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate!!)))
          Text(label)
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre, categoria, selectedDate) }) {
        Text(stringResource(R.string.dashboard_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) }
    }
  )
}
