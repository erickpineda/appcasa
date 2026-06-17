package com.appcasa.features.utilities.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
import com.appcasa.core.utils.Constants
import com.appcasa.feature.dashboard.R
import com.appcasa.features.utilities.presentation.viewmodel.UtilitiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val savedValues by viewModel.savedValues.collectAsStateWithLifecycle()
  
  var height by remember(savedValues) { mutableStateOf(savedValues[Constants.Keys.BMI_HEIGHT] ?: "") }
  var weight by remember(savedValues) { mutableStateOf(savedValues[Constants.Keys.BMI_WEIGHT] ?: "") }

  val h = height.toDoubleOrNull() ?: 0.0
  val w = weight.toDoubleOrNull() ?: 0.0
  val bmi = if (h > 0) w / ((h / 100.0) * (h / 100.0)) else 0.0

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.util_bmi_calculator)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
          }
        },
        actions = {
          TextButton(onClick = {
            viewModel.saveValue(Constants.Keys.BMI_HEIGHT, height)
            viewModel.saveValue(Constants.Keys.BMI_WEIGHT, weight)
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
        value = height,
        onValueChange = { height = it },
        label = { Text(stringResource(R.string.util_bmi_label_height)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
      )

      OutlinedTextField(
        value = weight,
        onValueChange = { weight = it },
        label = { Text(stringResource(R.string.util_bmi_label_weight)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
      )

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text(stringResource(R.string.util_bmi_result_prefix), style = MaterialTheme.typography.labelLarge)
          Text(
            text = String.format(Constants.Formatting.ONE_DECIMAL, bmi),
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
        stringResource(R.string.util_bmi_save_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline
      )
    }
  }
}

@Composable
private fun getBMICategory(bmi: Double): String {
  return when {
    bmi < 18.5 -> stringResource(R.string.util_bmi_cat_underweight)
    bmi < 25 -> stringResource(R.string.util_bmi_cat_normal)
    bmi < 30 -> stringResource(R.string.util_bmi_cat_overweight)
    else -> stringResource(R.string.util_bmi_cat_obese)
  }
}

