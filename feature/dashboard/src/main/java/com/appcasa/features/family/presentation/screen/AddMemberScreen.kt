package com.appcasa.features.family.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.data.utils.FileUtils
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.feature.dashboard.R
import com.appcasa.features.family.presentation.viewmodel.AddMemberViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
  navController: NavController,
  viewModel: AddMemberViewModel = hiltViewModel()
) {
  var nombre by remember { mutableStateOf("") }
  var nombreTouched by remember { mutableStateOf(false) }
  var tipo by remember { mutableStateOf(TipoMiembro.PERSONA) }
  var raza by remember { mutableStateOf("") }
  var color by remember { mutableStateOf("") }
  var chip by remember { mutableStateOf("") }
  var fotoUri by remember { mutableStateOf<String?>(null) }
  var expanded by remember { mutableStateOf(false) }
  
  var selectedBirthDate by remember { mutableStateOf<Long?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }
  val datePickerState = rememberDatePickerState()

  val confirmText = stringResource(R.string.family_btn_ok)

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          selectedBirthDate = datePickerState.selectedDateMillis
          showDatePicker = false
        }) { Text(confirmText) }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  val context = LocalContext.current

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri -> 
    uri?.let {
        fotoUri = FileUtils.saveImageLocally(context, it.toString())
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.family_add_member_title)) },
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
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Selector de Foto
      Box(
        modifier = Modifier
          .size(120.dp)
          .clip(CircleShape)
          .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
          .clickable { imagePickerLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
      ) {
        if (fotoUri != null) {
          AsyncImage(
            model = fotoUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
          // Overlay sutil para indicar que se puede cambiar
          Surface(
              color = Color.Black.copy(alpha = 0.3f),
              modifier = Modifier.fillMaxSize()
          ) {
              Box(contentAlignment = Alignment.Center) {
                  Icon(
                      Icons.Default.PhotoCamera, 
                      contentDescription = null, 
                      tint = Color.White.copy(alpha = 0.8f),
                      modifier = Modifier.size(32.dp)
                  )
              }
          }
        } else {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.family_label_photo), style = MaterialTheme.typography.labelSmall)
          }
        }
      }

      OutlinedTextField(
        value = nombre,
        onValueChange = { 
            nombre = it
            nombreTouched = true
        },
        label = { Text(stringResource(R.string.family_label_name)) },
        modifier = Modifier.fillMaxWidth(),
        isError = nombreTouched && nombre.isBlank(),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        supportingText = {
            if (nombreTouched && nombre.isBlank()) {
                Text(stringResource(R.string.family_error_name_required), color = MaterialTheme.colorScheme.error)
            }
        }
      )

      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
      ) {
        OutlinedTextField(
          value = tipo.name,
          onValueChange = {},
          readOnly = true,
          label = { Text(stringResource(R.string.family_label_type)) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          TipoMiembro.entries.forEach { entry ->
            DropdownMenuItem(
              text = { Text(entry.name) },
              onClick = {
                tipo = entry
                expanded = false
              }
            )
          }
        }
      }

      // Campo de Cumpleaños
      OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.Cake, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        val label = if (selectedBirthDate == null) "Añadir Cumpleaños" 
                    else "Cumpleaños: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedBirthDate!!))}"
        Text(label)
      }

      if (tipo != TipoMiembro.PERSONA) {
        OutlinedTextField(
          value = raza,
          onValueChange = { raza = it },
          label = { Text(stringResource(R.string.family_label_breed)) },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        OutlinedTextField(
          value = color,
          onValueChange = { color = it },
          label = { Text(stringResource(R.string.family_label_color)) },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        OutlinedTextField(
          value = chip,
          onValueChange = { chip = it },
          label = { Text(stringResource(R.string.family_label_chip)) },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )
      }

      Button(
        onClick = {
          viewModel.addMember(
            nombre = nombre, 
            tipo = tipo, 
            raza = raza.takeIf { it.isNotBlank() }, 
            color = color.takeIf { it.isNotBlank() },
            chip = chip.takeIf { it.isNotBlank() }, 
            fotoUri = fotoUri,
            fechaNacimiento = selectedBirthDate
          )
          navController.popBackStack()
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        enabled = nombre.isNotBlank()
      ) {
        Text(stringResource(R.string.family_btn_save))
      }

      // Espaciador dinámico avanzado para el teclado
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
    }
  }
}
