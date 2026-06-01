package com.appcasa.features.family.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.stringResource
import com.appcasa.feature.dashboard.R
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.data.utils.FileUtils
import com.appcasa.features.family.presentation.viewmodel.EditMemberViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemberScreen(
  navController: NavController,
  viewModel: EditMemberViewModel = hiltViewModel()
) {
  val member by viewModel.member.collectAsState()

  member?.let { currentMember ->
    var nombre by remember { mutableStateOf(currentMember.nombre) }
    var tipo by remember { mutableStateOf(TipoMiembro.valueOf(currentMember.tipo)) }
    var raza by remember { mutableStateOf(currentMember.raza ?: "") }
    var color by remember { mutableStateOf(currentMember.colorPelaje ?: "") }
    var chip by remember { mutableStateOf(currentMember.numeroChip ?: "") }
    var vetNombre by remember { mutableStateOf(currentMember.veterinarioNombre ?: "") }
    var vetTlf by remember { mutableStateOf(currentMember.veterinarioTelefono ?: "") }
    var fotoUri by remember { mutableStateOf<String?>(currentMember.fotoUri) }
    var expanded by remember { mutableStateOf(false) }
    
    var selectedBirthDate by remember { mutableStateOf(currentMember.fechaNacimiento) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedBirthDate)

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
          title = { Text(stringResource(R.string.family_edit_member_title)) },
          navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
          }
        )
      }
    ) { padding ->
      Column(
        modifier = Modifier
          .padding(padding)
          .padding(16.dp)
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
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
              Text(stringResource(R.string.family_btn_change_photo), style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        OutlinedTextField(
          value = nombre,
          onValueChange = { nombre = it },
          label = { Text(stringResource(R.string.family_label_name)) },
          modifier = Modifier.fillMaxWidth()
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
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text(stringResource(R.string.family_label_color)) },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = chip,
            onValueChange = { chip = it },
            label = { Text(stringResource(R.string.family_label_chip)) },
            modifier = Modifier.fillMaxWidth()
          )
          HorizontalDivider()
          Text(stringResource(R.string.family_section_vet), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
          OutlinedTextField(
            value = vetNombre,
            onValueChange = { vetNombre = it },
            label = { Text(stringResource(R.string.family_label_vet_name)) },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = vetTlf,
            onValueChange = { vetTlf = it },
            label = { Text(stringResource(R.string.family_label_vet_phone)) },
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            viewModel.updateMember(
              nombre = nombre,
              tipo = tipo,
              raza = raza.takeIf { it.isNotBlank() },
              color = color.takeIf { it.isNotBlank() },
              chip = chip.takeIf { it.isNotBlank() },
              vetNombre = vetNombre.takeIf { it.isNotBlank() },
              vetTlf = vetTlf.takeIf { it.isNotBlank() },
              fotoUri = fotoUri,
              fechaNacimiento = selectedBirthDate
            )
            navController.popBackStack()
          },
          modifier = Modifier.fillMaxWidth(),
          enabled = nombre.isNotBlank()
        ) {
          Text(stringResource(R.string.family_btn_save_changes))
        }
      }
    }
  } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
  }
}
