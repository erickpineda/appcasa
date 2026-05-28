package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MortgageCalculatorScreen(
    navController: NavController
) {
    var capital by remember { mutableStateOf("") }
    var interest by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }

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
                title = { Text("Calculadora Hipoteca") },
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = capital,
                onValueChange = { capital = it },
                label = { Text("Capital Prestado (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = interest,
                onValueChange = { interest = it },
                label = { Text("Interés Anual (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = years,
                onValueChange = { years = it },
                label = { Text("Plazo (Años)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Cuota Mensual", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${String.format("%.2f", monthlyPayment)} €",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total intereses: ${String.format("%.2f", totalInterest)} €",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
