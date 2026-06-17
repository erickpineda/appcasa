package com.appcasa.features.utilities.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MortgageCalculatorScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val savedValues by viewModel.savedValues.collectAsStateWithLifecycle()
  
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
        title = { Text(stringResource(R.string.util_mortgage_title)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
          }
        },
        actions = {
          TextButton(onClick = {
            viewModel.saveValue("MORTGAGE_CAPITAL", capital)
            viewModel.saveValue("MORTGAGE_INTEREST", interest)
            viewModel.saveValue("MORTGAGE_YEARS", years)
          }) {
            Text(stringResource(R.string.util_bmi_save), color = MaterialTheme.colorScheme.onPrimary)
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
        value = capital,
        onValueChange = { capital = it },
        label = { Text(stringResource(R.string.util_mortgage_label_capital)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )

      OutlinedTextField(
        value = interest,
        onValueChange = { interest = it },
        label = { Text(stringResource(R.string.util_mortgage_label_interest)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )

      OutlinedTextField(
        value = years,
        onValueChange = { years = it },
        label = { Text(stringResource(R.string.util_mortgage_label_term)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text(stringResource(R.string.util_mortgage_result_label), style = MaterialTheme.typography.labelLarge)
          Text(
            text = "${String.format("%.2f", monthlyPayment)} €",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = stringResource(R.string.util_mortgage_total_interest, String.format("%.2f", totalInterest)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }
      
      Text(
        stringResource(R.string.util_mortgage_save_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline
      )
    }
  }
}

