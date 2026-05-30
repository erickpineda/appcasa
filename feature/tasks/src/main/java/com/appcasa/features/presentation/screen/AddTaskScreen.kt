package com.appcasa.features.tasks.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.features.tasks.presentation.viewmodel.AddTaskViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
  navController: NavController,
  viewModel: AddTaskViewModel = hiltViewModel()
) {
  val familyMembers by viewModel.familyMembers.collectAsState()

  var titulo by remember { mutableStateOf("") }
  var tituloTouched by remember { mutableStateOf(false) }
  var descripcion by remember { mutableStateOf("") }
  var prioridad by remember { mutableStateOf(Prioridad.MEDIA) }
  var periodicidad by remember { mutableStateOf(Periodicidad.NINGUNA) }
  var tipoContenido by remember { mutableStateOf(TipoContenidoTarea.LISTA) }
  var esPersonal by remember { mutableStateOf(false) }
  var selectedMemberId by remember { mutableStateOf<Long?>(null) }
  var fotoUri by remember { mutableStateOf<String?>(null) }
  
  var memberExpanded by remember { mutableStateOf(false) }
  var repeatExpanded by remember { mutableStateOf(false) }
  
  // Estado para la fecha y hora
  var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
  var selectedAnticipacion by remember { mutableStateOf(0) }
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  val datePickerState = rememberDatePickerState()
  val timePickerState = rememberTimePickerState()

  val canSave = titulo.isNotBlank()

  // ... (rest of the dialogs unchanged)

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDateMillis = datePickerState.selectedDateMillis
          showDatePicker = false
          showTimePicker = true
        }) { Text("Siguiente (Hora)") }
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
          }
          showDatePicker = false 
        }) { Text("Todo el día") }
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
          val cal = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis ?: System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
          }
          selectedDateMillis = cal.timeInMillis
          showTimePicker = false
        }) { Text("OK") }
      },
      dismissButton = {
        TextButton(onClick = {
          val cal = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis ?: System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
          }
          selectedDateMillis = cal.timeInMillis
          showTimePicker = false
        }) { Text("Todo el día") }
      },
      title = { Text("Seleccionar Hora") },
      text = { TimePicker(state = timePickerState) }
    )
  }

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    fotoUri = uri?.toString()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Nueva Tarea") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .padding(16.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      OutlinedTextField(
        value = titulo,
        onValueChange = { 
            titulo = it
            tituloTouched = true
        },
        label = { Text("¿Qué hay que hacer?") },
        modifier = Modifier.fillMaxWidth(),
        isError = tituloTouched && titulo.isBlank(),
        supportingText = {
            if (tituloTouched && titulo.isBlank()) {
                Text("El título es obligatorio", color = MaterialTheme.colorScheme.error)
            }
        }
      )

      // Selector de Tipo
      Text("Tipo de tarea", style = MaterialTheme.typography.labelSmall)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TipoContenidoTarea.entries.forEach { tipo ->
          FilterChip(
            selected = tipoContenido == tipo,
            onClick = { tipoContenido = tipo },
            label = { Text(if (tipo == TipoContenidoTarea.LISTA) "Lista / Pasos" else "Nota de texto") },
            leadingIcon = {
                Icon(
                    imageVector = if (tipo == TipoContenidoTarea.LISTA) Icons.Default.List else Icons.Default.Notes,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            modifier = Modifier.weight(1f)
          )
        }
      }

      if (tipoContenido == TipoContenidoTarea.TEXTO) {
        OutlinedTextField(
          value = descripcion,
          onValueChange = { descripcion = it },
          label = { Text("Detalles de la tarea...") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3
        )
      }

      // Fecha Límite
      OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.CalendarToday, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        
        val dateLabel = if (selectedDateMillis == null) {
          "Añadir Fecha Límite (Opcional)"
        } else {
          val date = Date(selectedDateMillis!!)
          val cal = Calendar.getInstance().apply { time = date }
          val format = if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
            "dd/MM/yyyy '(Todo el día)'"
          } else {
            "dd/MM/yyyy HH:mm"
          }
          "Vence: ${SimpleDateFormat(format, Locale.getDefault()).format(date)}"
        }
        Text(dateLabel)
      }

      // Selector de Recurrencia
      ExposedDropdownMenuBox(
        expanded = repeatExpanded,
        onExpandedChange = { repeatExpanded = !repeatExpanded }
      ) {
        OutlinedTextField(
          value = periodicidad.name,
          onValueChange = {},
          readOnly = true,
          label = { Text("Repetir") },
          leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatExpanded) },
          modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
          expanded = repeatExpanded,
          onDismissRequest = { repeatExpanded = false }
        ) {
          Periodicidad.entries.forEach { p ->
            DropdownMenuItem(
              text = { Text(p.name) },
              onClick = {
                periodicidad = p
                repeatExpanded = false
              }
            )
          }
        }
      }

      if (selectedDateMillis != null) {
        Text("Avisar antes:", style = MaterialTheme.typography.labelSmall)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          val options = listOf(0 to "En punto", 5 to "5 min", 15 to "15 min", 30 to "30 min")
          options.forEach { (mins, label) ->
            FilterChip(
              selected = selectedAnticipacion == mins,
              onClick = { selectedAnticipacion = mins },
              label = { Text(label, style = MaterialTheme.typography.labelSmall) },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // Adjuntar Foto
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { imagePickerLauncher.launch("image/*") },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.PhotoCamera, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text(if (fotoUri == null) "Adjuntar Foto" else "Cambiar Foto")
        }
        
        fotoUri?.let { uri ->
          AsyncImage(
            model = uri,
            contentDescription = "Vista previa",
            modifier = Modifier
              .size(56.dp)
              .clip(RoundedCornerShape(8.dp))
              .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
          )
        }
      }

      Text("Asignar a", style = MaterialTheme.typography.titleMedium)
      ExposedDropdownMenuBox(
        expanded = memberExpanded,
        onExpandedChange = { memberExpanded = !memberExpanded }
      ) {
        val selectedName = familyMembers.find { it.id == selectedMemberId }?.nombre ?: "Sin asignar"
        OutlinedTextField(
          value = selectedName,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberExpanded) },
          modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
          expanded = memberExpanded,
          onDismissRequest = { memberExpanded = false }
        ) {
          DropdownMenuItem(text = { Text("Sin asignar") }, onClick = { selectedMemberId = null; memberExpanded = false })
          familyMembers.forEach { member ->
            DropdownMenuItem(
              text = { Text(member.nombre) },
              onClick = {
                selectedMemberId = member.id
                memberExpanded = false
              }
            )
          }
        }
      }

      Text("Prioridad", style = MaterialTheme.typography.titleMedium)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Prioridad.entries.forEach { p ->
          FilterChip(
            selected = prioridad == p,
            onClick = { prioridad = p },
            label = { Text(p.name) },
            modifier = Modifier.weight(1f)
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Checkbox(
          checked = esPersonal,
          onCheckedChange = { esPersonal = it }
        )
        Text("Es una tarea personal")
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = {
          viewModel.addTask(titulo, prioridad, selectedMemberId, esPersonal, fotoUri, selectedDateMillis, selectedAnticipacion, periodicidad, tipoContenido)
          navController.popBackStack()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = canSave
      ) {
        Text("Crear Tarea")
      }
    }
  }
}
