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
import com.appcasa.features.tasks.presentation.viewmodel.AddTaskViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
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
  var prioridad by remember { mutableStateOf(Prioridad.MEDIA) }
  var esPersonal by remember { mutableStateOf(false) }
  var selectedMemberId by remember { mutableStateOf<Long?>(null) }
  var fotoUri by remember { mutableStateOf<String?>(null) }
  var expanded by remember { mutableStateOf(false) }
  
  // Estado para la fecha
  var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }
  val datePickerState = rememberDatePickerState()

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDateMillis = datePickerState.selectedDateMillis
          showDatePicker = false
        }) { Text("OK") }
      }
    ) {
      DatePicker(state = datePickerState)
    }
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
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .padding(16.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      OutlinedTextField(
        value = titulo,
        onValueChange = { titulo = it },
        label = { Text("¿Qué hay que hacer?") },
        modifier = Modifier.fillMaxWidth()
      )

      // Fecha Límite
      OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.CalendarToday, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(
          if (selectedDateMillis == null) "Añadir Fecha Límite (Opcional)"
          else "Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDateMillis!!))}"
        )
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
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
      ) {
        val selectedName = familyMembers.find { it.id == selectedMemberId }?.nombre ?: "Sin asignar"
        OutlinedTextField(
          value = selectedName,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          DropdownMenuItem(text = { Text("Sin asignar") }, onClick = { selectedMemberId = null; expanded = false })
          familyMembers.forEach { member ->
            DropdownMenuItem(
              text = { Text(member.nombre) },
              onClick = {
                selectedMemberId = member.id
                expanded = false
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

      Spacer(modifier = Modifier.weight(1f))

      Button(
        onClick = {
          viewModel.addTask(titulo, prioridad, selectedMemberId, esPersonal, fotoUri, selectedDateMillis)
          navController.popBackStack()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = titulo.isNotBlank()
      ) {
        Text("Crear Tarea")
      }
    }
  }
}
