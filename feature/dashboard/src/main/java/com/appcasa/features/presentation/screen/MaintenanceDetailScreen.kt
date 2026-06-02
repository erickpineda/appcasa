package com.appcasa.features.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.features.presentation.viewmodel.HomeMaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceDetailScreen(
    id: Long,
    navController: NavController,
    viewModel: HomeMaintenanceViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    val event = events.find { it.id == id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.titulo ?: "Detalle de Mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (event != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppCasaCard(useGlassmorphism = true) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Categoría: ${event.categoria}", style = MaterialTheme.typography.titleMedium)
                        event.descripcion?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it)
                        }
                    }
                }
                
                // Aquí se podría mostrar el QR generado para este equipo
                Text("QR de Identificación", style = MaterialTheme.typography.labelLarge)
                AppCasaCard(useGlassmorphism = true, modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Build, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        Text("QR: maintenance/$id", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
