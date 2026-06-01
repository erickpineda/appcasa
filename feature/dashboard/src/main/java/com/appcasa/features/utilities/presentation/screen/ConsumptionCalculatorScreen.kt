package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.feature.dashboard.R
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
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .imePadding()
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
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      OutlinedTextField(
        value = currentReading,
        onValueChange = { currentReading = it },
        label = { Text(stringResource(R.string.util_consumption_label_curr)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      OutlinedTextField(
        value = pricePerKwh,
        onValueChange = { pricePerKwh = it },
        label = { Text(stringResource(R.string.util_consumption_label_price)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
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
