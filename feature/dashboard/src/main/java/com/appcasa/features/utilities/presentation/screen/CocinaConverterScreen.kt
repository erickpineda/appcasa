package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.feature.dashboard.R

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
        title = { Text(stringResource(R.string.util_kitchen_converter)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
            label = { Text(stringResource(R.string.util_kitchen_label_amount)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
          )
        }
        item {
          ConversionGroup(stringResource(R.string.util_kitchen_group_liquids))
          ConversionRow(stringResource(R.string.util_kitchen_cups_ml), "${String.format("%.1f", numValue * 240)} ml")
          ConversionRow(stringResource(R.string.util_kitchen_tbsp_ml), "${String.format("%.1f", numValue * 15)} ml")
          ConversionRow(stringResource(R.string.util_kitchen_tsp_ml), "${String.format("%.1f", numValue * 5)} ml")
        }
        item {
          ConversionGroup(stringResource(R.string.util_kitchen_group_weight))
          ConversionRow(stringResource(R.string.util_kitchen_flour_gr), "${String.format("%.1f", numValue * 120)} gr")
          ConversionRow(stringResource(R.string.util_kitchen_sugar_gr), "${String.format("%.1f", numValue * 200)} gr")
        }
        item {
          ConversionGroup(stringResource(R.string.util_kitchen_group_temp))
          ConversionRow(stringResource(R.string.util_kitchen_f_to_c), "${String.format("%.1f", (numValue - 32) * 5/9)} °C")
          ConversionRow(stringResource(R.string.util_kitchen_c_to_f), "${String.format("%.1f", (numValue * 9/5) + 32)} °F")
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
