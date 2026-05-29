package com.appcasa.features.tasks.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.features.tasks.data.local.TareaCheckItemEntity
import com.appcasa.features.tasks.presentation.viewmodel.TaskDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
  navController: NavController,
  viewModel: TaskDetailViewModel = hiltViewModel()
) {
  val task by viewModel.task.collectAsState()
  val subTasks by viewModel.subTasks.collectAsState()
  var newSubTaskText by remember { mutableStateOf("") }
  val haptic = LocalHapticFeedback.current
  
  var selectedItems by remember { mutableStateOf(setOf<Long>()) }
  val isSelectionMode = selectedItems.isNotEmpty()
  var showEditDialog by remember { mutableStateOf(false) }

  if (showEditDialog && task != null) {
    EditTaskMainDialog(
      titulo = task!!.titulo,
      descripcion = task!!.descripcion ?: "",
      prioridad = Prioridad.valueOf(task!!.prioridad),
      periodicidad = Periodicidad.valueOf(task!!.periodicidad),
      tipoContenido = TipoContenidoTarea.valueOf(task!!.tipoContenido),
      esPersonal = task!!.esPersonal,
      fechaLimite = task!!.fechaLimite,
      fotoUri = task!!.fotoUri,
      onDismiss = { showEditDialog = false },
      onConfirm = { t, d, p, per, perCont, esp, fecha, f, anticipacion ->
        viewModel.updateTask(t, d.takeIf { it.isNotBlank() }, p.name, esp, f, fecha, anticipacion, per, perCont)
        showEditDialog = false
      }
    )
  }

  PullToRefreshWrapper {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { 
            if (isSelectionMode) {
              Text("${selectedItems.size} seleccionados")
            } else {
              Text(task?.titulo ?: "Tarea") 
            }
          },
          navigationIcon = {
            if (isSelectionMode) {
              IconButton(onClick = { selectedItems = emptySet() }) {
                Icon(Icons.Default.Close, contentDescription = "Cancelar selección")
              }
            } else {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
              }
            }
          },
          actions = {
            if (isSelectionMode) {
              val selectedSubTasks = subTasks.filter { selectedItems.contains(it.id) }
              val allSelectedCompleted = selectedSubTasks.all { it.completado }

              IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.toggleSubTasksCompletion(selectedSubTasks, !allSelectedCompleted)
                selectedItems = emptySet()
              }) {
                Icon(
                  imageVector = if (allSelectedCompleted) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                  contentDescription = "Cambiar estado"
                )
              }

              IconButton(onClick = {
                if (selectedItems.size == subTasks.size) {
                  selectedItems = emptySet()
                } else {
                  selectedItems = subTasks.map { it.id }.toSet()
                }
              }) {
                Icon(
                  imageVector = if (selectedItems.size == subTasks.size) Icons.Default.Deselect else Icons.Default.SelectAll,
                  contentDescription = "Seleccionar todo"
                )
              }
              IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val toDelete = subTasks.filter { selectedItems.contains(it.id) }
                viewModel.deleteSubTasks(toDelete)
                selectedItems = emptySet()
              }) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar seleccionados", tint = MaterialTheme.colorScheme.error)
              }
            } else {
              IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar Tarea")
              }
            }
          }
        )
      }
    ) { padding ->
      Column(
        modifier = Modifier
          .padding(padding)
          .fillMaxSize()
      ) {
        task?.let { currentTask ->
          LazyColumn(
            modifier = Modifier.fillMaxSize()
          ) {
            item {
              AnimatedVisibility(visible = !isSelectionMode) {
                currentTask.fotoUri?.let { uri ->
                  key(uri) {
                    AsyncImage(
                      model = uri,
                      contentDescription = "Foto",
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                      contentScale = ContentScale.Crop
                    )
                  }
                }
              }
            }

            item {
              if (currentTask.tipoContenido == TipoContenidoTarea.TEXTO.name) {
                // MODO TEXTO: Mostrar descripción prominentemente
                if (!currentTask.descripcion.isNullOrBlank()) {
                  Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                      text = currentTask.descripcion ?: "",
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                } else {
                  Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Sin nota. Pulsa editar para añadir una.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                  }
                }
              } else {
                // MODO LISTA: Mostrar descripción pequeña (si existe) y luego la checklist
                if (!currentTask.descripcion.isNullOrBlank()) {
                  Text(
                    text = currentTask.descripcion ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                  )
                }

                Row(
                  modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "Pasos de la tarea",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                  
                  if (!isSelectionMode && subTasks.isNotEmpty()) {
                    TextButton(
                      onClick = { selectedItems = subTasks.map { it.id }.toSet() },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                      modifier = Modifier.height(28.dp)
                    ) {
                      Icon(Icons.Default.LibraryAddCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Seleccionar", style = MaterialTheme.typography.labelSmall)
                    }
                  }
                }
              }
            }

            if (currentTask.tipoContenido == TipoContenidoTarea.LISTA.name) {
              item {
                AnimatedVisibility(visible = !isSelectionMode) {
                  Row(
                    modifier = Modifier
                      .padding(horizontal = 16.dp, vertical = 4.dp)
                      .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedTextField(
                      value = newSubTaskText,
                      onValueChange = { newSubTaskText = it },
                      placeholder = { Text("Añadir paso...") },
                      modifier = Modifier.weight(1f),
                      singleLine = true,
                      textStyle = MaterialTheme.typography.bodyMedium,
                      colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                      )
                    )
                    FloatingActionButton(
                      onClick = {
                        if (newSubTaskText.isNotBlank()) {
                          viewModel.addSubTask(newSubTaskText)
                          newSubTaskText = ""
                        }
                      },
                      modifier = Modifier.size(40.dp),
                      elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                      Icon(Icons.Default.Add, contentDescription = "Añadir")
                    }
                  }
                }
              }

              items(subTasks, key = { it.id }) { item ->
                val isSelected = selectedItems.contains(item.id)
                CompactSubTaskItemEditable(
                  item = item,
                  isSelected = isSelected,
                  isSelectionMode = isSelectionMode,
                  onToggleSelection = {
                    selectedItems = if (isSelected) selectedItems - item.id else selectedItems + item.id
                  },
                  onToggle = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.toggleSubTask(item) 
                  },
                  onDelete = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.deleteSubTask(item) 
                  },
                  onEdit = { nuevoTexto -> viewModel.updateSubTask(item, nuevoTexto) }
                )
                HorizontalDivider(
                  modifier = Modifier.padding(horizontal = 16.dp),
                  thickness = 0.5.dp,
                  color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)
                )
              }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
          }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
    }
  }
}

@Composable
fun CompactSubTaskItemEditable(
  item: TareaCheckItemEntity,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  onToggleSelection: () -> Unit,
  onToggle: () -> Unit,
  onDelete: () -> Unit,
  onEdit: (String) -> Unit
) {
  var isEditing by remember { mutableStateOf(false) }
  var editedText by remember { mutableStateOf(item.texto) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { if (isSelectionMode) onToggleSelection() else onToggle() }
      .padding(horizontal = 4.dp, vertical = 0.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (isSelectionMode) {
      Checkbox(
        checked = isSelected,
        onCheckedChange = { onToggleSelection() },
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    } else {
      IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
        Icon(
          imageVector = if (item.completado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
          contentDescription = null,
          tint = if (item.completado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }
    
    if (isEditing && !isSelectionMode) {
      OutlinedTextField(
        value = editedText,
        onValueChange = { editedText = it },
        modifier = Modifier.weight(1f).padding(vertical = 2.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        trailingIcon = {
          Row {
            IconButton(onClick = { if (editedText.isNotBlank()) { onEdit(editedText); isEditing = false } }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Check, contentDescription = "OK", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { editedText = item.texto; isEditing = false }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Close, contentDescription = "X", modifier = Modifier.size(16.dp))
            }
          }
        }
      )
    } else {
      Text(
        text = item.texto,
        style = MaterialTheme.typography.bodyMedium,
        textDecoration = if (item.completado) TextDecoration.LineThrough else null,
        color = if (item.completado) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
          .weight(1f)
          .clickable(enabled = !isSelectionMode) { isEditing = true }
          .padding(vertical = 4.dp)
      )

      if (!isSelectionMode) {
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(
            Icons.Default.Delete, 
            contentDescription = "Borrar", 
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskMainDialog(
  titulo: String,
  descripcion: String,
  prioridad: Prioridad,
  periodicidad: Periodicidad,
  tipoContenido: TipoContenidoTarea,
  esPersonal: Boolean,
  fechaLimite: Long?,
  fotoUri: String?,
  onDismiss: () -> Unit,
  onConfirm: (String, String, Prioridad, Periodicidad, TipoContenidoTarea, Boolean, Long?, String?, Int) -> Unit
) {
  var t by remember { mutableStateOf(titulo) }
  var d by remember { mutableStateOf(descripcion) }
  var p by remember { mutableStateOf(prioridad) }
  var per by remember { mutableStateOf(periodicidad) }
  var perCont by remember { mutableStateOf<TipoContenidoTarea>(tipoContenido) }
  var esp by remember { mutableStateOf(esPersonal) }
  var f by remember { mutableStateOf(fotoUri) }
  
  var selectedFecha by remember { mutableStateOf(fechaLimite) }
  var selectedAnticipacion by remember { mutableStateOf(0) }
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  
  var repeatExpanded by remember { mutableStateOf(false) }
  
  val initialDate = fechaLimite ?: System.currentTimeMillis()
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
  val initialCal = Calendar.getInstance().apply { timeInMillis = initialDate }
  val timePickerState = rememberTimePickerState(initialHour = initialCal.get(Calendar.HOUR_OF_DAY), initialMinute = initialCal.get(Calendar.MINUTE))

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri -> f = uri?.toString() }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedFecha = datePickerState.selectedDateMillis
          showDatePicker = false
          showTimePicker = true
        }) { Text("Siguiente (Hora)") }
      },
      dismissButton = {
        TextButton(onClick = { 
          selectedFecha = datePickerState.selectedDateMillis?.let { 
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
            timeInMillis = selectedFecha ?: System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
          }
          selectedFecha = cal.timeInMillis
          showTimePicker = false
        }) { Text("OK") }
      },
      dismissButton = {
        TextButton(onClick = {
          val cal = Calendar.getInstance().apply {
            timeInMillis = selectedFecha ?: System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
          }
          selectedFecha = cal.timeInMillis
          showTimePicker = false
        }) { Text("Todo el día") }
      },
      title = { Text("Seleccionar Hora") },
      text = { TimePicker(state = timePickerState) }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Editar Tarea") },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        item {
          OutlinedTextField(value = t, onValueChange = { t = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
        }
        item {
          OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("Descripción / Nota") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        }

        item {
          Text("Tipo de Contenido", style = MaterialTheme.typography.labelSmall)
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TipoContenidoTarea.entries.forEach { tc: TipoContenidoTarea ->
              FilterChip(
                selected = perCont == tc,
                onClick = { perCont = tc },
                label = { Text(if (tc == TipoContenidoTarea.LISTA) "Lista" else "Nota", style = MaterialTheme.typography.labelSmall) }
              )
            }
          }
        }
        
        item {
          OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            val dateLabel = if (selectedFecha == null) {
              "Añadir Fecha Límite"
            } else {
              val cal = Calendar.getInstance().apply { timeInMillis = selectedFecha!! }
              if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
                SimpleDateFormat("dd/MM/yyyy '(Todo el día)'", Locale.getDefault()).format(Date(selectedFecha!!))
              } else {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(selectedFecha!!))
              }
            }
            Text(dateLabel)
          }
        }

        item {
          ExposedDropdownMenuBox(
            expanded = repeatExpanded,
            onExpandedChange = { repeatExpanded = !repeatExpanded }
          ) {
            OutlinedTextField(
              value = per.name,
              onValueChange = {},
              readOnly = true,
              label = { Text("Repetir") },
              leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(18.dp)) },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = repeatExpanded,
              onDismissRequest = { repeatExpanded = false }
            ) {
              Periodicidad.entries.forEach { pEntry ->
                DropdownMenuItem(
                  text = { Text(pEntry.name) },
                  onClick = {
                    per = pEntry
                    repeatExpanded = false
                  }
                )
              }
            }
          }
        }

        if (selectedFecha != null) {
          item {
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
        }

        item {
          OutlinedButton(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (f == null) "Añadir Imagen" else "Cambiar Imagen")
          }
        }
        
        item {
          Text("Prioridad", style = MaterialTheme.typography.labelSmall)
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Prioridad.entries.forEach { prio ->
              FilterChip(
                selected = p == prio,
                onClick = { p = prio },
                label = { Text(prio.name, style = MaterialTheme.typography.labelSmall) }
              )
            }
          }
        }
        
        item {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = esp, onCheckedChange = { esp = it })
            Text("Tarea personal", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (t.isNotBlank()) onConfirm(t, d, p, per, perCont, esp, selectedFecha, f, selectedAnticipacion) }) {
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    }
  )
}
