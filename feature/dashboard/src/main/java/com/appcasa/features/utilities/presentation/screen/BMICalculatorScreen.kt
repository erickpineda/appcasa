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
import com.appcasa.features.utilities.presentation.viewmodel.UtilitiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val savedValues by viewModel.savedValues.collectAsState()
  
  var height by remember(savedValues) { mutableStateOf(savedValues["BMI_HEIGHT"] ?: "") }
  var weight by remember(savedValues) { mutableStateOf(savedValues["BMI_WEIGHT"] ?: "") }

  val h = height.toDoubleOrNull() ?: 0.0
  val w = weight.toDoubleOrNull() ?: 0.0
  val bmi = if (h > 0) w / ((h / 100.0) * (h / 100.0)) else 0.0

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Calculadora IMC") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
          }
        },
        actions = {
          TextButton(onClick = {
            viewModel.saveValue("BMI_HEIGHT", height)
            viewModel.saveValue("BMI_WEIGHT", weight)
          }) {
            Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
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
      OutlinedTextField(
        value = height,
        onValueChange = { height = it },
        label = { Text("Altura (cm)") },
        modifier = Modifier.fillMaxWidth()
      )

      OutlinedTextField(
        value = weight,
        onValueChange = { weight = it },
        label = { Text("Peso (kg)") },
        modifier = Modifier.fillMaxWidth()
      )

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text("Tu IMC es", style = MaterialTheme.typography.labelLarge)
          Text(
            text = String.format("%.1f", bmi),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = getBMICategory(bmi),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }
      
      Text(
        "Nota: Los datos guardados aparecerán automáticamente la próxima vez que entres.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline
      )
    }
  }
}

private fun getBMICategory(bmi: Double): String {
  return when {
    bmi < 18.5 -> "Bajo peso"
    bmi < 25 -> "Normal"
    bmi < 30 -> "Sobrepeso"
    else -> "Obesidad"
  }
}
