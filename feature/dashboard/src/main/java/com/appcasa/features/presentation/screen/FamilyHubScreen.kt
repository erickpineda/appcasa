package com.appcasa.features.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyHubScreen(
  navController: NavController
) {
  AppCasaMeshBackground {
    PullToRefreshWrapper {
      Scaffold(
        topBar = {
          TopAppBar(
            title = { Text("Familia y Vida") },
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
            title = "Miembros",
            subtitle = "Humanos y pasaportes de mascotas",
            icon = Icons.Default.Groups,
            color = MaterialTheme.colorScheme.primary,
            onClick = { navController.navigate(Screen.Family.route) }
          )

          HubCard(
            title = "Agenda",
            subtitle = "Eventos, vacunas y turnos",
            icon = Icons.Default.CalendarMonth,
            color = MaterialTheme.colorScheme.secondary,
            onClick = { navController.navigate(Screen.Calendar.route) }
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
  color: Color,
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
