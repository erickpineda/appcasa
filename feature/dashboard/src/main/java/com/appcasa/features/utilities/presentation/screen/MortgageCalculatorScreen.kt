package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MortgageCalculatorScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val savedValues by viewModel.savedValues.collectAsState()
  
  var capital by remember(savedValues) { mutableStateOf(savedValues["MORTGAGE_CAPITAL"] ?: "") }
  var interest by remember(savedValues) { mutableStateOf(savedValues["MORTGAGE_INTEREST"] ?: "") }
  var years by remember(savedValues) { mutableStateOf(savedValues["MORTGAGE_YEARS"] ?: "") }

  val c = capital.toDoubleOrNull() ?: 0.0
  val i = (interest.toDoubleOrNull() ?: 0.0) / 100.0 / 12.0
  val n = (years.toIntOrNull() ?: 0) * 12

  val monthlyPayment = if (i > 0 && n > 0) {
    c * (i * (1 + i).pow(n)) / ((1 + i).pow(n) - 1)
  } else if (n > 0) {
    c / n
  } else 0.0

  val totalInterest = (monthlyPayment * n) - c

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Calculadora Hipoteca") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
          }
        },
        actions = {
          TextButton(onClick = {
            viewModel.saveValue("MORTGAGE_CAPITAL", capital)
            viewModel.saveValue("MORTGAGE_INTEREST", interest)
            viewModel.saveValue("MORTGAGE_YEARS", years)
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
        value = capital,
        onValueChange = { capital = it },
        label = { Text("Capital Prestado (€)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
      )

      OutlinedTextField(
        value = interest,
        onValueChange = { interest = it },
        label = { Text("Interés Anual (%)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
      )

      OutlinedTextField(
        value = years,
        onValueChange = { years = it },
        label = { Text("Plazo (Años)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
      )

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text("Cuota Mensual", style = MaterialTheme.typography.labelLarge)
          Text(
            text = "${String.format("%.2f", monthlyPayment)} €",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Total intereses: ${String.format("%.2f", totalInterest)} €",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }
      
      Text(
        "Nota: Los datos se guardan para facilitar consultas rápidas.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline
      )
    }
  }
}
