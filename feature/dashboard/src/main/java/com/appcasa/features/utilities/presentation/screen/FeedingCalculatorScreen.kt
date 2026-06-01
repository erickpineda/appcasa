package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
fun FeedingCalculatorScreen(
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
  var selectedPetType by remember { mutableStateOf(TipoMiembro.PERRO.name) }
  var expanded by remember { mutableStateOf(false) }

  var weightInput by remember { mutableStateOf("") }
  
  LaunchedEffect(petWeight) {
    if (petWeight > 0 && weightInput.toDoubleOrNull() != petWeight) {
        weightInput = if (petWeight % 1 == 0.0) petWeight.toInt().toString() else petWeight.toString()
    }
  }

  val ration = remember(petWeight, selectedPetType) {
    if (selectedPetType == TipoMiembro.GATO.name) {
        when {
            petWeight <= 0 -> "" // Placeholder, using stringResource below
            petWeight < 2 -> "CONSULT_VET_LOW"
            petWeight <= 4 -> "30 - 50 g"
            petWeight <= 6 -> "50 - 65 g"
            petWeight <= 8 -> "65 - 75 g"
            else -> "CONSULT_VET_HIGH_CAT"
        }
    } else {
        // Lógica para perros
        when {
            petWeight <= 0 -> ""
            petWeight <= 5 -> "25 - 90 g"
            petWeight <= 10 -> "90 - 150 g"
            petWeight <= 25 -> "150 - 300 g"
            petWeight <= 45 -> "300 - 465 g"
            petWeight <= 70 -> "465 - 650 g"
            else -> "CONSULT_VET_HIGH_DOG"
        }
    }
  }

  val rationText = when(ration) {
      "" -> stringResource(R.string.util_feeding_enter_weight)
      "CONSULT_VET_LOW" -> stringResource(R.string.util_feeding_consult_vet_low)
      "CONSULT_VET_HIGH_CAT" -> stringResource(R.string.util_feeding_consult_vet_high_cat)
      "CONSULT_VET_HIGH_DOG" -> stringResource(R.string.util_feeding_consult_vet_high_dog)
      else -> ration
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.util_feeding_title)) },
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
      Text(stringResource(R.string.util_feeding_header), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
      
      Text(stringResource(R.string.util_feeding_step1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
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
              text = { Text("${pet.nombre} (${pet.tipo})") },
              onClick = {
                selectedPetName = pet.nombre
                selectedPetType = pet.tipo
                dosageViewModel.updateWeightForPet(pet.id)
                expanded = false
              }
            )
          }
        }
      }

      Text(stringResource(R.string.util_feeding_step2), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
      OutlinedTextField(
        value = weightInput,
        onValueChange = { 
            weightInput = it
            dosageViewModel.setManualWeight(it.toDoubleOrNull() ?: 0.0)
        },
        label = { Text(stringResource(R.string.util_feeding_label_weight)) },
        suffix = { Text("kg") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
          Spacer(Modifier.height(16.dp))
          Text(stringResource(R.string.util_feeding_result_header), style = MaterialTheme.typography.labelLarge)
          Text(
            text = rationText,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
          )
          Spacer(Modifier.height(8.dp))
          Text(
            stringResource(R.string.util_feeding_manufacturer_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
        }
      }

      val petTypeLabel = if (selectedPetType == TipoMiembro.GATO.name) stringResource(R.string.util_feeding_gatos) else stringResource(R.string.util_feeding_perros)
      Text(stringResource(R.string.util_feeding_table_ref, petTypeLabel), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (selectedPetType == TipoMiembro.GATO.name) {
                ReferenceRow("2 - 4 kg", "30 - 50 g")
                ReferenceRow("4 - 6 kg", "50 - 65 g")
                ReferenceRow("6 - 8 kg", "65 - 75 g")
            } else {
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
