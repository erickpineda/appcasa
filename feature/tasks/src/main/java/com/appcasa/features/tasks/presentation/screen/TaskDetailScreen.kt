package com.appcasa.features.tasks.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryAddCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.TaskCheckItem
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.utils.Constants
import com.appcasa.feature.tasks.R
import com.appcasa.features.tasks.presentation.viewmodel.TaskDetailViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
  navController: NavController,
  viewModel: TaskDetailViewModel = hiltViewModel()
) {
  val task by viewModel.task.collectAsStateWithLifecycle()
  val assignedMember by viewModel.assignedMember.collectAsStateWithLifecycle()
  val subTasks by viewModel.subTasks.collectAsStateWithLifecycle()
  var newSubTaskText by remember { mutableStateOf("") }
  val haptic = LocalHapticFeedback.current
  
  val isTaskCompleted = task?.estado == EstadoTarea.COMPLETADA
  var selectedItems by remember { mutableStateOf(setOf<Long>()) }
  val isSelectionMode = selectedItems.isNotEmpty()
  var showEditDialog by remember { mutableStateOf(false) }

  if (showEditDialog && task != null) {
    EditTaskMainDialog(
      titulo = task!!.titulo,
      descripcion = task!!.descripcion ?: "",
      prioridad = task!!.prioridad,
      periodicidad = task!!.periodicidad,
      tipoContenido = task!!.tipoContenido,
      esPersonal = task!!.esPersonal,
      fechaLimite = task!!.fechaLimite,
      anticipacionActual = task!!.anticipacionMins,
      fotoUri = task!!.fotoUri,
      onDismiss = { showEditDialog = false },
      onConfirm = { t, d, p, per, perCont, esp, fecha, f, anticipacion ->
        viewModel.updateTask(t, d.takeIf { it.isNotBlank() }, p, esp, f, fecha, anticipacion, per, perCont)
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
            Text(stringResource(R.string.task_selected_count, selectedItems.size))
          } else {
            Text(task?.titulo ?: stringResource(R.string.task_fallback_title)) 
          }
        },
        navigationIcon = {
          if (isSelectionMode) {
            IconButton(onClick = { selectedItems = emptySet() }) {
              Icon(Icons.Default.Close, contentDescription = stringResource(R.string.task_cd_cancel_selection))
            }
          } else {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                contentDescription = stringResource(R.string.task_cd_change_status)
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
                contentDescription = stringResource(R.string.task_cd_select_all)
              )
            }
            IconButton(onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              val toDelete = subTasks.filter { selectedItems.contains(it.id) }
              viewModel.deleteSubTasks(toDelete)
              selectedItems = emptySet()
            }) {
              Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.task_cd_delete_selected), tint = MaterialTheme.colorScheme.error)
            }
          } else {
            if (!isTaskCompleted) {
              IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.task_edit_title))
              }
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
                      contentDescription = stringResource(R.string.task_cd_photo),
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
              Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Badges de Estado/Asignación
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isTaskCompleted) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.tasks_completed).uppercase()) },
                            icon = { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    SuggestionChip(
                        onClick = {},
                        label = { Text(currentTask.prioridad.name) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = when(currentTask.prioridad) {
                                Prioridad.ALTA -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    )
                    assignedMember?.let { member ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(member.nombre) },
                            icon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    if (currentTask.periodicidad != Periodicidad.NINGUNA) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(currentTask.periodicidad.name) },
                            icon = { Icon(Icons.Default.Repeat, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }

                if (currentTask.fechaLimite != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                      Spacer(Modifier.width(8.dp))
                      
                      val date = Date(currentTask.fechaLimite!!)
                      val cal = Calendar.getInstance().apply { time = date }
                      val format = if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
                        Constants.Formatting.DAY_MONTH_ALL_DAY_ES
                      } else {
                        Constants.Formatting.DAY_MONTH_TIME_ES
                      }
                      
                      Text(
                        text = stringResource(R.string.task_label_vence, SimpleDateFormat(format, Constants.Locales.SPAIN).format(date)),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isTaskCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                      )
                    }
                    
                    if (currentTask.anticipacionMins > 0) {
                        Text(
                            stringResource(R.string.task_label_aviso_antes, currentTask.anticipacionMins),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
              }
            }

            item {
              if (currentTask.tipoContenido == TipoContenidoTarea.TEXTO) {
                // MODO TEXTO: Mostrar descripción prominentemente
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!currentTask.descripcion.isNullOrBlank()) {
                        Text(
                            text = currentTask.descripcion ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isTaskCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (isTaskCompleted) TextDecoration.LineThrough else null
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.task_no_note), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
              } else {
                // MODO LISTA: Mostrar descripciÃ³n pequeÃ±a (si existe) y luego la checklist
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
                    text = stringResource(R.string.task_steps_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                  
                  if (!isSelectionMode && subTasks.isNotEmpty() && !isTaskCompleted) {
                    TextButton(
                      onClick = { selectedItems = subTasks.map { it.id }.toSet() },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                      modifier = Modifier.height(28.dp)
                    ) {
                      Icon(Icons.Default.LibraryAddCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(stringResource(R.string.task_btn_select), style = MaterialTheme.typography.labelSmall)
                    }
                  }
                }
              }
            }

            if (currentTask.tipoContenido == TipoContenidoTarea.LISTA) {
              item {
                AnimatedVisibility(visible = !isSelectionMode && !isTaskCompleted) {
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
                      placeholder = { Text(stringResource(R.string.task_placeholder_add_step)) },
                      modifier = Modifier.weight(1f),
                      singleLine = true,
                      textStyle = MaterialTheme.typography.bodyMedium,
                      keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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
                      Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
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
                  isParentTaskCompleted = isTaskCompleted,
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
  item: TaskCheckItem,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  isParentTaskCompleted: Boolean,
  onToggleSelection: () -> Unit,
  onToggle: () -> Unit,
  onDelete: () -> Unit,
  onEdit: (String) -> Unit
) {
  var isEditing by remember { mutableStateOf(false) }
  var editedText by remember { mutableStateOf(item.texto) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(isEditing) {
      if (isEditing) {
          delay(50)
          focusRequester.requestFocus()
          keyboardController?.show()
      }
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = !isParentTaskCompleted) { if (isSelectionMode) onToggleSelection() else onToggle() }
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
      IconButton(onClick = onToggle, enabled = !isParentTaskCompleted, modifier = Modifier.size(32.dp)) {
        Icon(
          imageVector = if (item.completado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
          contentDescription = null,
          tint = if (item.completado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }
    
    if (isEditing && !isSelectionMode && !isParentTaskCompleted) {
      OutlinedTextField(
        value = editedText,
        onValueChange = { editedText = it },
        modifier = Modifier.weight(1f).padding(vertical = 2.dp).focusRequester(focusRequester),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        trailingIcon = {
          Row {
            IconButton(onClick = { if (editedText.isNotBlank()) { onEdit(editedText); isEditing = false } }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Check, contentDescription = stringResource(R.string.task_ok), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { editedText = item.texto; isEditing = false }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Close, contentDescription = stringResource(R.string.task_cancel), modifier = Modifier.size(16.dp))
            }
          }
        }
      )
    } else {
      Text(
        text = item.texto,
        style = MaterialTheme.typography.bodyMedium,
        textDecoration = if (item.completado || isParentTaskCompleted) TextDecoration.LineThrough else null,
        color = if (item.completado || isParentTaskCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
          .weight(1f)
          .clickable(enabled = !isSelectionMode && !isParentTaskCompleted) { isEditing = true }
          .padding(vertical = 4.dp)
      )

      if (!isSelectionMode && !isParentTaskCompleted) {
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(
            Icons.Default.Delete, 
            contentDescription = stringResource(R.string.task_delete), 
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
  anticipacionActual: Int,
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
  var selectedAnticipacion by remember { mutableStateOf(anticipacionActual) }
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  
  var repeatExpanded by remember { mutableStateOf(false) }
  
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
      delay(300)
      focusRequester.requestFocus()
      keyboardController?.show()
  }
  
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
        }) { Text(stringResource(R.string.task_next_hour)) }
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
        }) { Text(stringResource(R.string.task_all_day)) }
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
        }) { Text(stringResource(R.string.task_ok)) }
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
        }) { Text(stringResource(R.string.task_all_day)) }
      },
      title = { Text(stringResource(R.string.task_select_hour)) },
      text = { TimePicker(state = timePickerState) }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.task_edit_title)) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        item {
          OutlinedTextField(
              value = t, 
              onValueChange = { t = it }, 
              label = { Text(stringResource(R.string.task_label_title)) }, 
              modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
              keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
          )
        }
        item {
          OutlinedTextField(
              value = d, 
              onValueChange = { d = it }, 
              label = { Text(stringResource(R.string.task_label_description)) }, 
              modifier = Modifier.fillMaxWidth(), 
              minLines = 2,
              keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
          )
        }

        item {
          Text(stringResource(R.string.task_content_type), style = MaterialTheme.typography.labelSmall)
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TipoContenidoTarea.entries.forEach { tc: TipoContenidoTarea ->
              FilterChip(
                selected = perCont == tc,
                onClick = { perCont = tc },
                label = { Text(if (tc == TipoContenidoTarea.LISTA) stringResource(R.string.task_content_type_list) else stringResource(R.string.task_content_type_note), style = MaterialTheme.typography.labelSmall) }
              )
            }
          }
        }
        
        item {
          OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            val dateLabel = if (selectedFecha == null) {
              stringResource(R.string.task_btn_add_deadline)
            } else {
              val cal = Calendar.getInstance().apply { timeInMillis = selectedFecha!! }
              if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
                SimpleDateFormat("${Constants.Formatting.DATE_FORMAT_ES} '${stringResource(R.string.task_all_day)}'", Locale.getDefault()).format(Date(selectedFecha!!))
              } else {
                SimpleDateFormat(Constants.Formatting.DATETIME_FORMAT_ES, Locale.getDefault()).format(Date(selectedFecha!!))
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
              label = { Text(stringResource(R.string.task_repeat)) },
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
            Text(stringResource(R.string.task_notify_before), style = MaterialTheme.typography.labelSmall)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              val options = listOf(
                0 to stringResource(R.string.task_notify_on_time), 
                5 to stringResource(R.string.task_notify_5_min), 
                15 to stringResource(R.string.task_notify_15_min), 
                30 to stringResource(R.string.task_notify_30_min)
              )
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
          OutlinedButton(onClick = { imagePickerLauncher.launch(Constants.Media.MIME_TYPE_IMAGE) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (f == null) stringResource(R.string.task_btn_add_image) else stringResource(R.string.task_btn_change_image))
          }
        }
        
        item {
          Text(stringResource(R.string.task_priority), style = MaterialTheme.typography.labelSmall)
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
            Text(stringResource(R.string.task_label_personal), style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (t.isNotBlank()) onConfirm(t, d, p, per, perCont, esp, selectedFecha, f, selectedAnticipacion) }) {
        Text(stringResource(R.string.task_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.task_cancel)) }
    }
  )
}

