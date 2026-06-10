package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.feature.dashboard.R
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
  
  var selectedPetName by remember { mutableStateOf("") }
  val selectPetHint = stringResource(R.string.util_dosage_select_pet)
  
  if (selectedPetName.isEmpty()) {
      selectedPetName = selectPetHint
  }
  var petType by remember { mutableStateOf(TipoMiembro.PERRO) }
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
        title = { Text(stringResource(R.string.util_pet_dosage)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary,
          navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(stringResource(R.string.util_dosage_section_pet), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
      
      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
      ) {
        OutlinedTextField(
          value = selectedPetName,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          modifier = Modifier.menuAnchor().fillMaxWidth(),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          pets.forEach { pet ->
            DropdownMenuItem(
              text = { Text("${pet.nombre} (${pet.tipo.name})") },
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

      if (petType == TipoMiembro.TORTUGA) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))) {
            Text(
                stringResource(R.string.util_dosage_turtle_warning),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
      }

      Text(stringResource(R.string.util_dosage_section_dose), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
      
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = weightInput,
          onValueChange = { 
              weightInput = it
              dosageViewModel.setManualWeight(it.toDoubleOrNull() ?: 0.0)
          },
          label = { Text(stringResource(R.string.util_dosage_label_manual_weight)) },
          modifier = Modifier.weight(1f),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
          value = doseValueInput,
          onValueChange = { doseValueInput = it },
          label = { Text(stringResource(R.string.util_dosage_label_dose)) },
          modifier = Modifier.weight(1f),
          singleLine = true,
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
        Text(stringResource(R.string.util_dosage_is_liquid))
      }

      if (isLiquid) {
        Text(stringResource(R.string.util_dosage_section_concentration), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = concentrationMg,
            onValueChange = { concentrationMg = it },
            label = { Text(stringResource(R.string.util_dosage_label_total_mg)) },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ej: 100") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
          OutlinedTextField(
            value = concentrationMl,
            onValueChange = { concentrationMl = it },
            label = { Text(stringResource(R.string.util_dosage_label_each_ml)) },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ej: 5") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
        }
      }

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Text(stringResource(R.string.util_dosage_result_title), style = MaterialTheme.typography.labelLarge)
          Spacer(Modifier.height(8.dp))
          
          if (isLiquid && finalMl > 0) {
            Text(
              text = stringResource(R.string.util_dosage_unit_ml_format, String.format("%.2f", finalMl)),
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.ExtraBold
            )
            Text(stringResource(R.string.util_dosage_equiv_mg, String.format("%.1f", totalDoseMg)), style = MaterialTheme.typography.bodySmall)
          } else {
            val displayValue = if (totalDoseMg >= 1000) totalDoseMg / 1000 else totalDoseMg
            val displayRes = if (totalDoseMg >= 1000) R.string.util_dosage_unit_gr_format else R.string.util_dosage_unit_mg_format
            
            Text(
              text = stringResource(displayRes, String.format("%.1f", displayValue)),
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.ExtraBold
            )
            if (totalDoseMg < 1.0 && totalDoseMg > 0) {
                Text(text = stringResource(R.string.util_dosage_unit_mcg, String.format("%.0f", totalDoseMg * 1000)), style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }

      Text(
        stringResource(R.string.util_dosage_vet_notice),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}
