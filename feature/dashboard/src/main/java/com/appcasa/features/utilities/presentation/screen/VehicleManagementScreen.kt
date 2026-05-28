package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Vehículo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it },
                label = { Text("Matrícula") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = insurance,
                onValueChange = { insurance = it },
                label = { Text("Compañía de Seguro") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono Asistencia") },
                trailingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.saveVehicleData(plate, insurance, phone)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Información")
            }
        }
    }
}
