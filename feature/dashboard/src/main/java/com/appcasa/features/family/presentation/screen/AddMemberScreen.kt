package com.appcasa.features.family.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.features.family.presentation.viewmodel.AddMemberViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
  navController: NavController,
  viewModel: AddMemberViewModel = hiltViewModel()
) {
  var nombre by remember { mutableStateOf("") }
  var tipo by remember { mutableStateOf(TipoMiembro.PERSONA) }
  var raza by remember { mutableStateOf("") }
  var color by remember { mutableStateOf("") }
  var chip by remember { mutableStateOf("") }
  var fotoUri by remember { mutableStateOf<String?>(null) }
  var expanded by remember { mutableStateOf(false) }

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri -> fotoUri = uri?.toString() }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Añadir Miembro") },
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
        } else {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Foto", style = MaterialTheme.typography.labelSmall)
          }
        }
      }

      OutlinedTextField(
        value = nombre,
        onValueChange = { nombre = it },
        label = { Text("Nombre") },
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
          label = { Text("Tipo") },
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

      if (tipo != TipoMiembro.PERSONA) {
        OutlinedTextField(
          value = raza,
          onValueChange = { raza = it },
          label = { Text("Raza / Especie") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = color,
          onValueChange = { color = it },
          label = { Text("Color / Descripción") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = chip,
          onValueChange = { chip = it },
          label = { Text("Número de Chip") },
          modifier = Modifier.fillMaxWidth()
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      Button(
        onClick = {
          viewModel.addMember(
            nombre = nombre, 
            tipo = tipo, 
            raza = raza.takeIf { it.isNotBlank() }, 
            color = color.takeIf { it.isNotBlank() },
            chip = chip.takeIf { it.isNotBlank() }, 
            fotoUri = fotoUri
          )
          navController.popBackStack()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = nombre.isNotBlank()
      ) {
        Text("Guardar Miembro")
      }
    }
  }
}
