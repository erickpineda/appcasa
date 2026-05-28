package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionCalculatorScreen(
    navController: NavController
) {
    var previousReading by remember { mutableStateOf("") }
    var currentReading by remember { mutableStateOf("") }
    var pricePerKwh by remember { mutableStateOf("") }

    val prev = previousReading.toDoubleOrNull() ?: 0.0
    val curr = currentReading.toDoubleOrNull() ?: 0.0
    val price = pricePerKwh.toDoubleOrNull() ?: 0.0
    
    val totalConsumption = (curr - prev).coerceAtLeast(0.0)
    val totalCost = totalConsumption * price

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consumo Eléctrico") },
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
                value = previousReading,
                onValueChange = { previousReading = it },
                label = { Text("Lectura Anterior (kWh)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = currentReading,
                onValueChange = { currentReading = it },
                label = { Text("Lectura Actual (kWh)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pricePerKwh,
                onValueChange = { pricePerKwh = it },
                label = { Text("Precio por kWh (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Resumen Estimado", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${String.format("%.2f", totalCost)} €",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = "Consumo: ${String.format("%.1f", totalConsumption)} kWh",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
