package com.appcasa.features.utilities.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.stringResource
import com.appcasa.feature.dashboard.R
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.features.utilities.presentation.viewmodel.UtilitiesViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val savedValues by viewModel.savedValues.collectAsStateWithLifecycle()
  
  var selectedMillis by remember(savedValues) { 
    mutableStateOf(savedValues["AGE_CALC_MILLIS"]?.toLongOrNull()) 
  }
  
  var ageResult by remember { mutableStateOf("") }
  val selectHint = stringResource(R.string.util_age_select_hint)
  val resultFormat = stringResource(R.string.util_age_result_format)
  
  if (ageResult.isEmpty() && selectedMillis == null) {
    ageResult = selectHint
  }

  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedMillis)
  var showDatePicker by remember { mutableStateOf(false) }

  // Calcular edad si hay fecha guardada
  LaunchedEffect(selectedMillis) {
    selectedMillis?.let {
      val selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
      val now = LocalDate.now()
      val period = java.time.Period.between(selectedDate, now)
      ageResult = java.lang.String.format(resultFormat, period.years, period.months, period.days)
    }
  }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          datePickerState.selectedDateMillis?.let {
            selectedMillis = it
            viewModel.saveValue("AGE_CALC_MILLIS", it.toString())
          }
          showDatePicker = false
        }) { Text(stringResource(R.string.family_btn_ok)) }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.util_age_title)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Event, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(if (selectedMillis == null) stringResource(R.string.util_age_btn_select) else stringResource(R.string.util_age_btn_change))
      }

      AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(stringResource(R.string.util_age_label_exact), style = MaterialTheme.typography.labelLarge)
          Spacer(Modifier.height(12.dp))
          Text(
            text = ageResult,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    }
  }
}
