package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.stringResource
import com.appcasa.feature.dashboard.R
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun ConsumptionCalculatorScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val savedValues by viewModel.savedValues.collectAsState()
  
  var previousReading by remember(savedValues) { mutableStateOf(savedValues["CONS_PREV"] ?: "") }
  var currentReading by remember(savedValues) { mutableStateOf(savedValues["CONS_CURR"] ?: "") }
  var pricePerKwh by remember(savedValues) { mutableStateOf(savedValues["CONS_PRICE"] ?: "") }

  val prev = previousReading.toDoubleOrNull() ?: 0.0
  val curr = currentReading.toDoubleOrNull() ?: 0.0
  val price = pricePerKwh.toDoubleOrNull() ?: 0.0
  
  val totalConsumption = (curr - prev).coerceAtLeast(0.0)
  val totalCost = totalConsumption * price

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.util_consumption_title)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
          }
        },
        actions = {
          TextButton(onClick = {
            viewModel.saveValue("CONS_PREV", previousReading)
            viewModel.saveValue("CONS_CURR", currentReading)
            viewModel.saveValue("CONS_PRICE", pricePerKwh)
          }) {
            Text(stringResource(R.string.dashboard_save), color = MaterialTheme.colorScheme.onPrimary)
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
        value = previousReading,
        onValueChange = { previousReading = it },
        label = { Text(stringResource(R.string.util_consumption_label_prev)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
      )
      OutlinedTextField(
        value = currentReading,
        onValueChange = { currentReading = it },
        label = { Text(stringResource(R.string.util_consumption_label_curr)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
      )
      OutlinedTextField(
        value = pricePerKwh,
        onValueChange = { pricePerKwh = it },
        label = { Text(stringResource(R.string.util_consumption_label_price)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
      )

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text(stringResource(R.string.util_consumption_result_header), style = MaterialTheme.typography.labelLarge)
          Text(
            text = "${String.format("%.2f", totalCost)} €",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = stringResource(R.string.util_consumption_result_format, String.format("%.1f", totalConsumption)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }
    }
  }
}
