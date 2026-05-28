package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(
    navController: NavController
) {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val h = height.toDoubleOrNull() ?: 0.0
    val w = weight.toDoubleOrNull() ?: 0.0
    val bmi = if (h > 0) w / ((h / 100.0) * (h / 100.0)) else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora IMC") },
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
                value = height,
                onValueChange = { height = it },
                label = { Text("Altura (cm)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Peso (kg)") },
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Tu IMC es", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = String.format("%.1f", bmi),
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = getBMICategory(bmi),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun getBMICategory(bmi: Double): String {
    return when {
        bmi < 18.5 -> "Bajo peso"
        bmi < 25 -> "Normal"
        bmi < 30 -> "Sobrepeso"
        else -> "Obesidad"
    }
}
