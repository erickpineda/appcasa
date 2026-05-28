package com.appcasa.features.family.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.features.family.presentation.viewmodel.FamilyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
  navController: NavController,
  memberId: Long,
  viewModel: FamilyViewModel = hiltViewModel()
) {
  val familyMembers by viewModel.familyMembers.collectAsState()
  val member = familyMembers.find { it.id == memberId }

  PullToRefreshWrapper {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text(member?.nombre ?: "Miembro") },
          navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
          },
          actions = {
            IconButton(onClick = { 
              member?.let { navController.navigate(com.appcasa.navigation.Screen.EditMember.createRoute(it.id)) }
            }) {
              Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
          }
        )
      }
    ) { padding ->
      member?.let { currentMember ->
        Column(
          modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Foto de Perfil
          Surface(
            modifier = Modifier.size(140.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 4.dp
          ) {
            Box(contentAlignment = Alignment.Center) {
              if (currentMember.fotoUri != null) {
                AsyncImage(
                  model = currentMember.fotoUri,
                  contentDescription = null,
                  modifier = Modifier.fillMaxSize().clip(CircleShape),
                  contentScale = ContentScale.Crop
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  modifier = Modifier.size(80.dp),
                  tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(24.dp))
          
          Text(
            text = currentMember.nombre,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Perfil Familiar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
          )

          Spacer(modifier = Modifier.height(32.dp))

          // Tarjeta de Detalles
          com.appcasa.core.ui.components.AppCasaCard(
            useGlassmorphism = true,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              DetailRow(icon = Icons.Default.Category, label = "Tipo", value = currentMember.tipo)
              currentMember.raza?.let { DetailRow(icon = Icons.Default.Pets, label = "Raza", value = it) }
              currentMember.numeroChip?.let { DetailRow(icon = Icons.Default.Tag, label = "Nº Chip", value = it) }
            }
          }

          if (!currentMember.veterinarioNombre.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            com.appcasa.core.ui.components.AppCasaCard(
              useGlassmorphism = true,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Información de Contacto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                DetailRow(icon = Icons.Default.LocalHospital, label = "Clínica/Vet", value = currentMember.veterinarioNombre ?: "")
                currentMember.veterinarioTelefono?.let { DetailRow(icon = Icons.Default.Phone, label = "Teléfono", value = it) }
              }
            }
          }
        }
      } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }
  }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
    Column {
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
  }
}
