package com.appcasa.features.family.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.components.skeletonShimmer
import com.appcasa.features.family.R
import com.appcasa.features.family.presentation.viewmodel.FamilyViewModel
import com.appcasa.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
  navController: NavController,
  memberId: Long,
  viewModel: FamilyViewModel = hiltViewModel()
) {
  val familyMembers by viewModel.familyMembers.collectAsState()
  val member = familyMembers.find { it.id == memberId }
  val scrollState = rememberScrollState()

  AppCasaMeshBackground {
    PullToRefreshWrapper {
      Scaffold(
        topBar = {
          TopAppBar(
            title = { Text(member?.nombre ?: stringResource(R.string.dashboard_member_fallback)) },
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
              }
            },
            actions = {
              IconButton(onClick = { 
                member?.let { navController.navigate(Screen.EditMember(it.id)) }
              }) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
              }
            }
          )
        },
        containerColor = Color.Transparent
      ) { padding ->
        if (member != null) {
          Column(
            modifier = Modifier
              .padding(padding)
              .fillMaxSize()
              .verticalScroll(scrollState)
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Foto de Perfil con Parallax
            Surface(
              modifier = Modifier
                .size(140.dp)
                .graphicsLayer {
                    translationY = scrollState.value * 0.4f
                    alpha = 1f - (scrollState.value.toFloat() / 500f).coerceIn(0f, 1f)
                },
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primaryContainer,
              shadowElevation = 4.dp
            ) {
              Box(contentAlignment = Alignment.Center) {
                if (member.fotoUri != null) {
                  AsyncImage(
                    model = member.fotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                  )
                } else {
                  Icon(
                    imageVector = if (member.tipo == TipoMiembro.PERSONA) Icons.Default.Person else Icons.Default.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
              text = member.nombre,
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (member.tipo == TipoMiembro.PERSONA) stringResource(R.string.dashboard_family_member_label) else stringResource(R.string.dashboard_pet_profile_label),
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de Detalles
            AppCasaCard(
              useGlassmorphism = true,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(icon = Icons.Default.Category, label = stringResource(R.string.family_label_type), value = member.tipo.name)
                
                if (member.fechaNacimiento != null) {
                  val dateStr = SimpleDateFormat("dd 'de' MMMM", Locale("es", "ES")).format(Date(member.fechaNacimiento!!))
                  DetailRow(icon = Icons.Default.Cake, label = stringResource(R.string.dashboard_birthday_label), value = dateStr)
                }

                if (!member.raza.isNullOrBlank()) {
                  DetailRow(icon = Icons.Default.Pets, label = stringResource(R.string.family_label_breed), value = member.raza!!)
                }
                
                if (!member.colorPelaje.isNullOrBlank()) {
                  DetailRow(icon = Icons.Default.Palette, label = stringResource(R.string.family_label_color), value = member.colorPelaje!!)
                }
                
                if (!member.numeroChip.isNullOrBlank()) {
                  DetailRow(icon = Icons.Default.Tag, label = stringResource(R.string.dashboard_chip_short_label), value = member.numeroChip!!)
                }
              }
            }

            if (!member.veterinarioNombre.isNullOrBlank() || !member.veterinarioTelefono.isNullOrBlank()) {
              Spacer(modifier = Modifier.height(16.dp))
              AppCasaCard(
                useGlassmorphism = true,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  Text(stringResource(R.string.family_contact_info), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                  
                  if (!member.veterinarioNombre.isNullOrBlank()) {
                    DetailRow(icon = Icons.Default.LocalHospital, label = stringResource(R.string.dashboard_vet_clinic_label), value = member.veterinarioNombre!!)
                  }
                  
                  if (!member.veterinarioTelefono.isNullOrBlank()) {
                    DetailRow(icon = Icons.Default.Phone, label = stringResource(R.string.dashboard_phone_label), value = member.veterinarioTelefono!!)
                  }
                }
              }
            }
          }
        } else {
          Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Box(modifier = Modifier.size(140.dp).clip(CircleShape).skeletonShimmer())
            Box(modifier = Modifier.width(200.dp).height(32.dp).clip(MaterialTheme.shapes.medium).skeletonShimmer())
            Box(modifier = Modifier.width(150.dp).height(20.dp).clip(MaterialTheme.shapes.small).skeletonShimmer())
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.large).skeletonShimmer())
          }
        }
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
