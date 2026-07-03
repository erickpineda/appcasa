package com.appcasa.features.tasks.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.core.utils.Constants
import com.appcasa.feature.tasks.R
import com.appcasa.features.tasks.presentation.viewmodel.AddTaskViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
  navController: NavController,
  viewModel: AddTaskViewModel = hiltViewModel()
) {
  val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()

  var titulo by remember { mutableStateOf("") }
  var tituloTouched by remember { mutableStateOf(false) }
  var descripcion by remember { mutableStateOf("") }
  var prioridad by remember { mutableStateOf(Prioridad.MEDIA) }
  var periodicidad by remember { mutableStateOf(Periodicidad.NINGUNA) }
  var tipoContenido by remember { mutableStateOf(TipoContenidoTarea.LISTA) }
  var esPersonal by remember { mutableStateOf(false) }
  var selectedMemberId by remember { mutableStateOf<String?>(null) }
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
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    delay(100)
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  val canSave = titulo.isNotBlank()

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedDateMillis = datePickerState.selectedDateMillis
          showDatePicker = false
          showTimePicker = true
        }) { Text(stringResource(R.string.task_next_hour)) }
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
            timeInMillis = selectedDateMillis ?: System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
          }
          selectedDateMillis = cal.timeInMillis
          showTimePicker = false
        }) { Text(stringResource(R.string.task_ok)) }
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
        }) { Text(stringResource(R.string.task_all_day)) }
      },
      title = { Text(stringResource(R.string.task_select_hour)) },
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
        title = { Text(stringResource(R.string.task_add_title)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = MaterialTheme.colorScheme.onPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      OutlinedTextField(
        value = titulo,
        onValueChange = { 
          titulo = it
          tituloTouched = true
        },
        label = { Text(stringResource(R.string.task_label_title)) },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        isError = tituloTouched && titulo.isBlank(),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        supportingText = {
          if (tituloTouched && titulo.isBlank()) {
            Text(stringResource(R.string.task_error_title_required), color = MaterialTheme.colorScheme.error)
          }
        }
      )

      // Selector de Tipo
      Text(stringResource(R.string.task_type), style = MaterialTheme.typography.labelSmall)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TipoContenidoTarea.entries.forEach { tipo ->
          FilterChip(
            selected = tipoContenido == tipo,
            onClick = { tipoContenido = tipo },
            label = { Text(if (tipo == TipoContenidoTarea.LISTA) stringResource(R.string.task_label_content_type_list) else stringResource(R.string.task_label_content_type_note)) },
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
          label = { Text(stringResource(R.string.task_details_placeholder)) },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
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
          stringResource(R.string.task_label_add_deadline_optional)
        } else {
          val date = Date(selectedDateMillis!!)
          val cal = Calendar.getInstance().apply { time = date }
          val format = if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
            "${Constants.Formatting.DATE_FORMAT_ES} '${stringResource(R.string.task_all_day)}'"
          } else {
            Constants.Formatting.DATETIME_FORMAT_ES
          }
          stringResource(R.string.task_label_vence, SimpleDateFormat(format, Locale.getDefault()).format(date))
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
          label = { Text(stringResource(R.string.task_repeat)) },
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

      // Adjuntar Foto
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { imagePickerLauncher.launch(Constants.Media.MIME_TYPE_IMAGE) },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.PhotoCamera, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text(if (fotoUri == null) stringResource(R.string.task_label_attach_photo) else stringResource(R.string.task_label_change_photo))
        }
        
        fotoUri?.let { uri ->
          AsyncImage(
            model = uri,
            contentDescription = stringResource(R.string.task_label_preview),
            modifier = Modifier
              .size(56.dp)
              .clip(RoundedCornerShape(8.dp))
              .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
          )
        }
      }

      Text(stringResource(R.string.task_assign_to), style = MaterialTheme.typography.titleMedium)
      ExposedDropdownMenuBox(
        expanded = memberExpanded,
        onExpandedChange = { memberExpanded = !memberExpanded }
      ) {
        val unassignedLabel = stringResource(R.string.task_unassigned)
        val selectedName = familyMembers.find { it.id == selectedMemberId }?.nombre ?: unassignedLabel
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
          DropdownMenuItem(text = { Text(unassignedLabel) }, onClick = { selectedMemberId = null; memberExpanded = false })
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

      Text(stringResource(R.string.task_priority), style = MaterialTheme.typography.titleMedium)
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

      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = esPersonal,
            onCheckedChange = { esPersonal = it }
          )
          Text(stringResource(R.string.task_label_personal_long))
        }
        if (esPersonal) {
          Text(
            text = stringResource(R.string.task_personal_xp_warning),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 48.dp, end = 16.dp)
          )
        }
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
        Text(stringResource(R.string.task_create_button))
      }
      
      // Espaciador dinámico avanzado para el teclado
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
    }
  }
}
