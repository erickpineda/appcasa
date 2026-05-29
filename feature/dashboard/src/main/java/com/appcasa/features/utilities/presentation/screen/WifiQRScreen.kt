package com.appcasa.features.utilities.presentation.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiQRScreen(
  navController: NavController
) {
  var ssid by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Compartir WiFi") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      AppCasaCard(useGlassmorphism = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it },
            label = { Text("Nombre de la Red (SSID)") },
            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
          )
          
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { showPassword = !showPassword }) {
                Icon(if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
              }
            },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      if (ssid.isNotBlank()) {
        Text("Escanea para conectarte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        AppCasaCard(
          modifier = Modifier.size(240.dp),
          useGlassmorphism = true
        ) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Aquí iría el generador de QR. Usamos un placeholder visual elegante.
            Icon(
              Icons.Default.QrCode, 
              contentDescription = "QR Placeholder", 
              modifier = Modifier.size(180.dp),
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Text(
              "WIFI:S:$ssid;T:WPA;P:$password;;", 
              style = MaterialTheme.typography.labelSmall,
              modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
              color = MaterialTheme.colorScheme.outline
            )
          }
        }
        
        Text(
          "Formato estándar de conexión automática", 
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Text(
            "Introduce el nombre de tu red para generar el código",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.outline
          )
        }
      }
    }
  }
}
