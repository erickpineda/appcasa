package com.appcasa.features.utilities.presentation.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.features.utilities.presentation.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleManagementScreen(
  navController: NavController,
  viewModel: VehicleViewModel = hiltViewModel()
) {
  val data by viewModel.vehicleData.collectAsState()

  var plate by remember(data) { mutableStateOf(data["VEH_PLATE"] ?: "") }
  var insurance by remember(data) { mutableStateOf(data["VEH_INSURANCE"] ?: "") }
  var phone by remember(data) { mutableStateOf(data["VEH_INSURANCE_PHONE"] ?: "") }
  var model by remember(data) { mutableStateOf(data["VEH_MODEL"] ?: "") }
  var year by remember(data) { mutableStateOf(data["VEH_YEAR"] ?: "") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.util_vehicle_title)) },
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
      Text(
        stringResource(R.string.util_vehicle_section_data), 
        style = MaterialTheme.typography.titleMedium, 
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
      )

      com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text(stringResource(R.string.util_vehicle_label_model)) },
            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = plate,
            onValueChange = { plate = it },
            label = { Text(stringResource(R.string.util_vehicle_label_plate)) },
            leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text(stringResource(R.string.util_vehicle_label_year)) },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Text(
        stringResource(R.string.util_vehicle_section_insurance), 
        style = MaterialTheme.typography.titleMedium, 
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
      )

      com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = insurance,
            onValueChange = { insurance = it },
            label = { Text(stringResource(R.string.util_vehicle_label_insurance_company)) },
            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.util_vehicle_label_assistance_phone)) },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = {
          viewModel.saveVehicleData(plate, insurance, phone, model, year)
          navController.popBackStack()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
      ) {
        Icon(Icons.Default.Save, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.util_vehicle_btn_save_all))
      }
    }
  }
}
