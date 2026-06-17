package com.appcasa.features.dashboard.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.utils.QRUtils
import com.appcasa.feature.dashboard.R
import com.appcasa.features.dashboard.presentation.viewmodel.HomeMaintenanceViewModel
import com.appcasa.core.ui.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceDetailScreen(
    id: Long,
    navController: NavController,
    viewModel: HomeMaintenanceViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val event = events.find { it.id == id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.titulo ?: stringResource(R.string.maintenance_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
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
                        Text(stringResource(R.string.maintenance_category_format, event.categoria), style = MaterialTheme.typography.titleMedium)
                        event.descripcion?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it)
                        }
                    }
                }
                
                Text(stringResource(R.string.maintenance_qr_ident_label), style = MaterialTheme.typography.labelLarge)
                AppCasaCard(useGlassmorphism = true, modifier = Modifier.size(240.dp).align(Alignment.CenterHorizontally)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val qrContent = "appcasa://maintenance/$id"
                        val qrBitmap = remember(id) { QRUtils.generateQRCode(qrContent, 400) }
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = stringResource(CoreR.string.cd_qr),
                                modifier = Modifier.size(200.dp)
                            )
                        } else {
                            Icon(Icons.Default.Build, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        }
                        Text(qrContent, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
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

