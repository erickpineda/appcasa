package com.appcasa.features.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.feature.dashboard.R
import com.appcasa.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementHubScreen(
  navController: NavController
) {
  AppCasaMeshBackground {
    PullToRefreshWrapper {
      Scaffold(
        topBar = {
          TopAppBar(
            title = { Text(stringResource(R.string.hub_management_title)) },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.primary,
              titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
          )
        },
        containerColor = Color.Transparent
      ) { padding ->
        Column(
          modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          HubCard(
            title = stringResource(R.string.hub_tasks_title),
            subtitle = stringResource(R.string.hub_tasks_subtitle),
            icon = Icons.Default.CheckCircle,
            color = MaterialTheme.colorScheme.primary,
            onClick = { navController.navigate(Screen.Tasks.route) }
          )

          HubCard(
            title = stringResource(R.string.hub_lists_title),
            subtitle = stringResource(R.string.hub_lists_subtitle),
            icon = Icons.Default.List,
            color = MaterialTheme.colorScheme.secondary,
            onClick = { navController.navigate(Screen.Lists.route) }
          )

          HubCard(
            title = stringResource(R.string.hub_inventory_title),
            subtitle = stringResource(R.string.hub_inventory_subtitle),
            icon = Icons.Default.Inventory,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = { navController.navigate(Screen.Inventory.route) }
          )

          HubCard(
            title = stringResource(R.string.hub_maintenance_title),
            subtitle = stringResource(R.string.hub_maintenance_subtitle),
            icon = Icons.Default.HomeRepairService,
            color = MaterialTheme.colorScheme.error,
            onClick = { navController.navigate(Screen.HomeMaintenance.route) }
          )

          HubCard(
            title = "Tienda de Recompensas",
            subtitle = "Canjea tus XP por premios familiares",
            icon = Icons.Default.CardGiftcard,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = { navController.navigate(Screen.RewardStore.route) }
          )
        }
      }
    }
  }
}

@Composable
private fun HubCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  color: androidx.compose.ui.graphics.Color,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth().height(120.dp),
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
  ) {
    Row(
      modifier = Modifier.padding(24.dp).fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(40.dp))
      Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}
