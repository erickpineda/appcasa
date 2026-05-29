package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.features.family.presentation.viewmodel.FamilyViewModel
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
  var petType by remember { mutableStateOf(TipoMiembro.PERRO.name) }
  var doseValueInput by remember { mutableStateOf("") }
  
  // Estado local para el peso para evitar que se resetee al escribir
  var weightInput by remember { mutableStateOf("") }
  
  // Sincronizar el input local cuando el peso cambia desde el VM (al seleccionar mascota)
  LaunchedEffect(petWeight) {
    if (petWeight > 0 && weightInput.toDoubleOrNull() != petWeight) {
        weightInput = if (petWeight % 1 == 0.0) petWeight.toInt().toString() else petWeight.toString()
    }
  }

  // Selector de unidad para la dosis (mg/kg, g/kg, mcg/kg)
  var doseUnit by remember { mutableStateOf("mg/kg") }
  
  // Para cálculo de líquido (ml)
  var isLiquid by remember { mutableStateOf(false) }
  var concentrationMg by remember { mutableStateOf("") }
  var concentrationMl by remember { mutableStateOf("") }
  
  var expanded by remember { mutableStateOf(false) }

  val doseValue = doseValueInput.toDoubleOrNull() ?: 0.0
  val totalDoseMg = when(doseUnit) {
    "g/kg" -> doseValue * 1000 * petWeight
    "µg/kg" -> (doseValue / 1000) * petWeight
    else -> doseValue * petWeight // mg/kg
  }

  val finalMl = if (isLiquid && (concentrationMg.toDoubleOrNull() ?: 0.0) > 0) {
    (totalDoseMg * (concentrationMl.toDoubleOrNull() ?: 1.0)) / (concentrationMg.toDoubleOrNull() ?: 1.0)
  } else 0.0

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Dosis Mascotas") },
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
      Text("1. Mascota y Especie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
      
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
              text = { Text("${pet.nombre} (${pet.tipo})") },
              onClick = {
                selectedPetName = pet.nombre
                petType = pet.tipo
                dosageViewModel.updateWeightForPet(pet.id)
                expanded = false
              }
            )
          }
        }
      }

      if (petType == TipoMiembro.TORTUGA.name) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))) {
            Text(
                "🐢 Atención: Las tortugas tienen un metabolismo muy lento. Confirma siempre con un experto en exóticos.",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
      }

      Text("2. Peso y Dosis Recomendada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
      
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = weightInput,
          onValueChange = { 
              weightInput = it
              dosageViewModel.setManualWeight(it.toDoubleOrNull() ?: 0.0)
          },
          label = { Text("Peso (kg)") },
          modifier = Modifier.weight(1f),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
          value = doseValueInput,
          onValueChange = { doseValueInput = it },
          label = { Text("Dosis") },
          modifier = Modifier.weight(1f),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
      }

      // Selector de UNIDAD de dosis
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        listOf("mg/kg", "g/kg", "µg/kg").forEach { unit ->
          FilterChip(
            selected = doseUnit == unit,
            onClick = { doseUnit = unit },
            label = { Text(unit, style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.weight(1f)
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = isLiquid, onCheckedChange = { isLiquid = it })
        Text("Es un medicamento líquido (Jarabe)")
      }

      if (isLiquid) {
        Text("3. Concentración del Bote", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = concentrationMg,
            onValueChange = { concentrationMg = it },
            label = { Text("mg totales") },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ej: 100") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
          OutlinedTextField(
            value = concentrationMl,
            onValueChange = { concentrationMl = it },
            label = { Text("en cada (ml)") },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ej: 5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
        }
      }

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Text("DOSIS FINAL CALCULADA", style = MaterialTheme.typography.labelLarge)
          Spacer(Modifier.height(8.dp))
          
          if (isLiquid && finalMl > 0) {
            Text(
              text = "${String.format("%.2f", finalMl)} ml",
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.ExtraBold
            )
            Text("Equivale a ${String.format("%.1f", totalDoseMg)} mg", style = MaterialTheme.typography.bodySmall)
          } else {
            val displayValue = if (totalDoseMg >= 1000) totalDoseMg / 1000 else totalDoseMg
            val displayUnit = if (totalDoseMg >= 1000) "gr" else "mg"
            
            Text(
              text = "${String.format("%.1f", displayValue)} $displayUnit",
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.ExtraBold
            )
            if (totalDoseMg < 1.0 && totalDoseMg > 0) {
                Text(text = "(${String.format("%.0f", totalDoseMg * 1000)} µg)", style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }

      Text(
        "Aviso: Consulta siempre con tu veterinario antes de medicar.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}
