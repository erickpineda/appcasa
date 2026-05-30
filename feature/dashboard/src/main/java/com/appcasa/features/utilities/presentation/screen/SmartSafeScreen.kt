package com.appcasa.features.utilities.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.features.documents.data.local.DocumentoEntity
import com.appcasa.features.utilities.presentation.viewmodel.SmartSafeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSafeScreen(
  navController: NavController,
  viewModel: SmartSafeViewModel = hiltViewModel()
) {
  val isUnlocked by viewModel.isUnlocked.collectAsState()
  val context = LocalContext.current
  
  LaunchedEffect(Unit) {
    if (!isUnlocked) {
        viewModel.authenticate(context as FragmentActivity)
    }
  }

  if (!isUnlocked) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
              Spacer(Modifier.height(16.dp))
              Text("Baúl bloqueado", style = MaterialTheme.typography.titleLarge)
              TextButton(onClick = { viewModel.authenticate(context as FragmentActivity) }) {
                  Text("Desbloquear con biometría")
              }
          }
      }
      return
  }

  val documentos by viewModel.documentos.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }
  var editingDocument by remember { mutableStateOf<DocumentoEntity?>(null) }

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
        title = { Text("Smart Safe") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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
        Icon(Icons.Default.Add, contentDescription = "Añadir Documento")
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
          "Tus documentos importantes",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      if (documentos.isEmpty()) {
        item {
          AppCasaEmptyState(
            title = "El baúl está vacío",
            description = "Añade documentos importantes como garantías, seguros o certificados.",
            icon = Icons.Default.FolderOpen,
            actionText = "Añadir Documento",
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
                    context.startActivity(Intent.createChooser(intent, "Abrir documento"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onEdit = { editingDocument = doc },
            onDelete = { viewModel.deleteDocumento(doc) },
            onCloudSync = { viewModel.uploadToCloud(doc) }
          )
        }
      }
    }
  }
}

@Composable
fun DocumentCard(
  documento: DocumentoEntity,
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
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha))
            Text("Vence: $date", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
          }
        }
      },
      leadingContent = {
        val icon = when(documento.categoria) {
          "Escolares" -> Icons.Default.School
          "Garantías" -> Icons.Default.VerifiedUser
          "Salud" -> Icons.Default.LocalHospital
          "Seguros" -> Icons.Default.Shield
          else -> Icons.Default.Description
        }
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      },
      trailingContent = {
        Row {
          IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
          }
          if (!documento.sincronizado) {
            IconButton(onClick = onCloudSync) {
              Icon(Icons.Default.CloudUpload, contentDescription = "Subir a la nube", tint = MaterialTheme.colorScheme.outline)
            }
          } else {
            Icon(Icons.Default.CloudDone, contentDescription = "Sincronizado", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(12.dp))
          }
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
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
  var nombre by remember { mutableStateOf("") }
  var categoria by remember { mutableStateOf("Garantías") }
  var selectedUri by remember { mutableStateOf<Uri?>(null) }
  
  var showDatePicker by remember { mutableStateOf(false) }
  val datePickerState = rememberDatePickerState()
  var selectedDate by remember { mutableStateOf<Long?>(null) }
  
  val categories = listOf("Garantías", "Escolares", "Salud", "Seguros", "Otros")
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
        }) { Text("OK") }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Nuevo Documento") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del documento") }, modifier = Modifier.fillMaxWidth())
        
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
          OutlinedTextField(
            value = categoria,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
          Text(if (selectedUri != null) "PDF Seleccionado" else "Elegir Archivo PDF")
        }

        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
          val label = if (selectedDate == null) "Añadir Vencimiento (Opcional)" else "Vence: " + SimpleDateFormat("dd/MM/yyyy").format(Date(selectedDate!!))
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
fun EditDocumentDialog(
  documento: DocumentoEntity,
  onDismiss: () -> Unit,
  onConfirm: (String, String, Long?) -> Unit
) {
  var nombre by remember { mutableStateOf(documento.nombre) }
  var categoria by remember { mutableStateOf(documento.categoria) }
  var showDatePicker by remember { mutableStateOf(false) }
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = documento.fechaVencimiento)
  var selectedDate by remember { mutableStateOf(documento.fechaVencimiento) }
  
  val categories = listOf("Garantías", "Escolares", "Salud", "Seguros", "Otros")
  var expanded by remember { mutableStateOf(false) }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDate = datePickerState.selectedDateMillis
          showDatePicker = false
        }) { Text("OK") }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Editar Documento") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
          OutlinedTextField(
            value = categoria,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { cat ->
              DropdownMenuItem(text = { Text(cat) }, onClick = { categoria = cat; expanded = false })
            }
          }
        }

        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
          val label = if (selectedDate == null) "Añadir Vencimiento" else "Vence: " + SimpleDateFormat("dd/MM/yyyy").format(Date(selectedDate!!))
          Text(label)
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre, categoria, selectedDate) }) {
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    }
  )
}
