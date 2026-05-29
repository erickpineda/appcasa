package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.features.family.presentation.viewmodel.FamilyViewModel
import com.appcasa.features.utilities.presentation.viewmodel.DosageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingCalculatorScreen(
  navController: NavController,
  familyViewModel: FamilyViewModel = hiltViewModel(),
  dosageViewModel: DosageViewModel = hiltViewModel()
) {
  val pets by familyViewModel.pets.collectAsState()
  val petWeight by dosageViewModel.petWeight.collectAsState()
  
  var selectedPetName by remember { mutableStateOf("Seleccionar Perro") }
  var expanded by remember { mutableStateOf(false) }

  // Estado local para evitar que se borre al escribir rápido o poner decimales
  var weightInput by remember { mutableStateOf("") }
  
  // Sincronizar el input local cuando el peso cambia desde el VM (al seleccionar mascota)
  LaunchedEffect(petWeight) {
    if (petWeight > 0 && weightInput.toDoubleOrNull() != petWeight) {
        weightInput = if (petWeight % 1 == 0.0) petWeight.toInt().toString() else petWeight.toString()
    }
  }

  val ration = when {
    petWeight <= 0 -> "Introduce un peso"
    petWeight <= 5 -> "25 - 90 g"
    petWeight <= 10 -> "90 - 150 g"
    petWeight <= 25 -> "150 - 300 g"
    petWeight <= 45 -> "300 - 465 g"
    petWeight <= 70 -> "465 - 650 g"
    else -> "Consultar veterinario (>70kg)"
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Ración de Pienso") },
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
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text("Guía de Alimentación Diaria", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
      
      Text("1. Elige a tu perro", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
      ) {
        OutlinedTextField(
          value = selectedPetName,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          pets.filter { it.tipo == "PERRO" }.forEach { pet ->
            DropdownMenuItem(
              text = { Text(pet.nombre) },
              onClick = {
                selectedPetName = pet.nombre
                dosageViewModel.updateWeightForPet(pet.id)
                expanded = false
              }
            )
          }
        }
      }

      Text("2. Peso actual (kg)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
      OutlinedTextField(
        value = weightInput,
        onValueChange = { 
            weightInput = it
            val weight = it.toDoubleOrNull() ?: 0.0
            dosageViewModel.setManualWeight(weight)
        },
        label = { Text("Peso") },
        suffix = { Text("kg") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
      )

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
          Spacer(Modifier.height(16.dp))
          Text("RACIÓN DIARIA RECOMENDADA", style = MaterialTheme.typography.labelLarge)
          Text(
            text = ration,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
          )
          Spacer(Modifier.height(8.dp))
          Text(
            "Repartir en 2 o 3 tomas al día",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
        }
      }

      Text("Tabla de referencia (Bolsa)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ReferenceRow("1 - 5 kg", "25 - 90 g")
            ReferenceRow("5 - 10 kg", "90 - 150 g")
            ReferenceRow("10 - 25 kg", "150 - 300 g")
            ReferenceRow("25 - 45 kg", "300 - 465 g")
            ReferenceRow("45 - 70 kg", "465 - 650 g")
        }
      }
    }
  }
}

@Composable
fun ReferenceRow(weight: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(weight, style = MaterialTheme.typography.bodySmall)
        Text(amount, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
