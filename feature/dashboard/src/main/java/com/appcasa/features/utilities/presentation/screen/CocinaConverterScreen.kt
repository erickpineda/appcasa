package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocinaConverterScreen(
  navController: NavController
) {
  var value by remember { mutableStateOf("") }
  val numValue = value.toDoubleOrNull() ?: 0.0

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Conversor de Cocina") },
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
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
          OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Cantidad a convertir") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
          )
        }
        item {
          ConversionGroup("Líquidos / Volumen")
          ConversionRow("Tazas a ml", "${String.format("%.1f", numValue * 240)} ml")
          ConversionRow("Cucharadas a ml", "${String.format("%.1f", numValue * 15)} ml")
          ConversionRow("Cucharaditas a ml", "${String.format("%.1f", numValue * 5)} ml")
        }
        item {
          ConversionGroup("Peso (Aproximado)")
          ConversionRow("Taza harina a gr", "${String.format("%.1f", numValue * 120)} gr")
          ConversionRow("Taza azúcar a gr", "${String.format("%.1f", numValue * 200)} gr")
        }
        item {
          ConversionGroup("Temperatura")
          ConversionRow("Fahrenheit a Celsius", "${String.format("%.1f", (numValue - 32) * 5/9)} °C")
          ConversionRow("Celsius a Fahrenheit", "${String.format("%.1f", (numValue * 9/5) + 32)} °F")
        }
      }
    }
  }
}

@Composable
private fun ConversionGroup(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.primary,
    fontWeight = FontWeight.Bold,
    modifier = Modifier.padding(vertical = 8.dp)
  )
}

@Composable
private fun ConversionRow(label: String, result: String) {
  AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Row(
      modifier = Modifier.padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
      Text(label, style = MaterialTheme.typography.bodyMedium)
      Text(result, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
  }
}
