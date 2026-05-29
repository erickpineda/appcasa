package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.features.utilities.presentation.viewmodel.UtilitiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsCalculatorScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val savedValues by viewModel.savedValues.collectAsState()
  
  var goalAmount by remember(savedValues) { mutableStateOf(savedValues["SAVINGS_GOAL"] ?: "") }
  var months by remember(savedValues) { mutableStateOf(savedValues["SAVINGS_MONTHS"] ?: "") }

  val goal = goalAmount.toDoubleOrNull() ?: 0.0
  val m = months.toIntOrNull() ?: 0
  val monthlySaving = if (m > 0) goal / m else 0.0

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Ahorro Mensual") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
          }
        },
        actions = {
          TextButton(onClick = {
            viewModel.saveValue("SAVINGS_GOAL", goalAmount)
            viewModel.saveValue("SAVINGS_MONTHS", months)
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
        value = goalAmount,
        onValueChange = { goalAmount = it },
        label = { Text("Objetivo total (€)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
      )
      OutlinedTextField(
        value = months,
        onValueChange = { months = it },
        label = { Text("Plazo (Meses)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
      )

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text("Necesitas ahorrar al mes", style = MaterialTheme.typography.labelLarge)
          Text(
            text = "${String.format("%.2f", monthlySaving)} €",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}
