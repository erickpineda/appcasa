package com.appcasa.features.utilities.presentation.screen

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
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.features.family.presentation.viewmodel.FamilyViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.appcasa.features.utilities.presentation.viewmodel.DosageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosageCalculatorScreen(
  navController: NavController,
  familyViewModel: FamilyViewModel = hiltViewModel(),
  dosageViewModel: DosageViewModel = hiltViewModel()
) {
  val pets by familyViewModel.pets.collectAsState()
  val petWeight by dosageViewModel.petWeight.collectAsState()
  
  var selectedPetName by remember { mutableStateOf("Seleccionar Mascota") }
  var mgPerKg by remember { mutableStateOf("") }
  var expanded by remember { mutableStateOf(false) }

  val totalDose = (mgPerKg.toDoubleOrNull() ?: 0.0) * petWeight

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Dosis Mascotas") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
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
      Text("1. Selecciona la mascota", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
      
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
          pets.forEach { pet ->
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

      Text("2. Peso de la mascota (kg)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
      OutlinedTextField(
        value = if (petWeight == 0.0) "" else petWeight.toString(),
        onValueChange = { dosageViewModel.setManualWeight(it.toDoubleOrNull() ?: 0.0) },
        label = { Text("Peso") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
      )

      Text("3. Parámetros médicos", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
      OutlinedTextField(
        value = mgPerKg,
        onValueChange = { mgPerKg = it },
        label = { Text("Dosis recomendada (mg/kg)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
      )

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text("Dosis Total Calculada", style = MaterialTheme.typography.labelLarge)
          Text(
            text = "${String.format("%.2f", totalDose)} mg",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}
