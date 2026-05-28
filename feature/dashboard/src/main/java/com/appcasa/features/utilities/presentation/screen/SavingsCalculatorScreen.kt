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
fun SavingsCalculatorScreen(
    navController: NavController
) {
    var goalAmount by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }

    val goal = goalAmount.toDoubleOrNull() ?: 0.0
    val m = months.toIntOrNull() ?: 0
    val monthlySaving = if (m > 0) goal / m else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ahorro Mensual") },
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
                value = goalAmount,
                onValueChange = { goalAmount = it },
                label = { Text("Objetivo total (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = months,
                onValueChange = { months = it },
                label = { Text("Plazo (Meses)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Necesitas ahorrar al mes", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${String.format("%.2f", monthlySaving)} €",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}
