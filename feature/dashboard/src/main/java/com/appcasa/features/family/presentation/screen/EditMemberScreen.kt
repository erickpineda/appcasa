package com.appcasa.features.family.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.features.family.presentation.viewmodel.EditMemberViewModel

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

    val imagePickerLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
    ) { uri -> fotoUri = uri?.toString() }

    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text("Editar Miembro") },
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
              Text("Cambiar Foto", style = MaterialTheme.typography.labelSmall)
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
          HorizontalDivider()
          Text("Veterinario", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
          OutlinedTextField(
            value = vetNombre,
            onValueChange = { vetNombre = it },
            label = { Text("Nombre Clínica/Vet") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = vetTlf,
            onValueChange = { vetTlf = it },
            label = { Text("Teléfono Veterinario") },
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
              fotoUri = fotoUri
            )
            navController.popBackStack()
          },
          modifier = Modifier.fillMaxWidth(),
          enabled = nombre.isNotBlank()
        ) {
          Text("Guardar Cambios")
        }
      }
    }
  } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
  }
}
