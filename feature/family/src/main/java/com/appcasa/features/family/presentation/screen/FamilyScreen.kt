package com.appcasa.features.family.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.model.isPet
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PremiumProgressBar
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.features.family.R
import com.appcasa.features.family.presentation.viewmodel.FamilyViewModel
import com.appcasa.navigation.Screen

@Composable
fun FamilyScreen(
  navController: NavController,
  viewModel: FamilyViewModel = hiltViewModel()
) {
  val people by viewModel.people.collectAsStateWithLifecycle()
  val pets by viewModel.pets.collectAsStateWithLifecycle()
  
  var memberToDelete by remember { mutableStateOf<FamilyMember?>(null) }

  AppCasaConfirmDialog(
    show = memberToDelete != null,
    title = stringResource(R.string.dashboard_delete_title_fallback, "Miembro"), // Added fallback title to strings.xml if needed, but let's use a better one
    text = stringResource(R.string.dashboard_delete_confirm_fallback, memberToDelete?.nombre ?: ""), 
    onConfirm = {
        memberToDelete?.let { viewModel.deleteMember(it) }
        memberToDelete = null
    },
    onDismiss = { memberToDelete = null }
  )

  AppCasaMeshBackground {
    PullToRefreshWrapper {
      FamilyContent(
        navController = navController,
        people = people,
        pets = pets,
        onAddClick = { navController.navigate(Screen.AddMember) },
        onDeleteMember = { memberToDelete = it },
        onMemberClick = { member ->
          if (member.isPet) {
            navController.navigate(Screen.PetDetail(member.id))
          } else {
            navController.navigate(Screen.MemberDetail(member.id))
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyContent(
  navController: NavController,
  people: List<FamilyMember>,
  pets: List<FamilyMember>,
  onAddClick: () -> Unit,
  onDeleteMember: (FamilyMember) -> Unit,
  onMemberClick: (FamilyMember) -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.family_title)) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    containerColor = Color.Transparent, // Para ver el MeshBackground
    floatingActionButton = {
      FloatingActionButton(onClick = onAddClick) {
        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.family_btn_add))
      }
    }
  ) { scaffoldPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(scaffoldPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (people.isEmpty() && pets.isEmpty()) {
        item {
          AppCasaEmptyState(
            title = stringResource(R.string.dashboard_empty_household_title), // Adding these to strings.xml
            description = stringResource(R.string.dashboard_empty_household_desc),
            icon = Icons.Default.Groups,
            actionText = stringResource(R.string.family_btn_add_first),
            onActionClick = onAddClick,
            modifier = Modifier.fillParentMaxSize()
          )
        }
      }

      if (people.isNotEmpty()) {
        item {
          Text(
            text = stringResource(R.string.dashboard_family_members),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
        }
        items(people) { person ->
          MemberCard(
            member = person,
            icon = Icons.Default.Person,
            onDelete = { onDeleteMember(person) },
            onClick = { onMemberClick(person) }
          )
        }
      }

      if (pets.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.dashboard_pets_title, pets.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
          )
        }
        items(pets) { pet ->
          MemberCard(
            member = pet,
            icon = Icons.Default.Pets,
            onDelete = { onDeleteMember(pet) },
            onClick = { onMemberClick(pet) }
          )
        }
      }
    }
  }
}

@Composable
fun MemberCard(
  member: FamilyMember,
  icon: ImageVector,
  onDelete: () -> Unit,
  onClick: () -> Unit
) {
  AppCasaCard(useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth().alpha(0.9f),
    onClick = onClick
  ) {
    Row(
      modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Surface(
        modifier = Modifier.size(60.dp), // Aumentado para que la foto luzca más
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 2.dp
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
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(32.dp),
              tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = member.nombre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          if (member.tipo == TipoMiembro.PERSONA) {
            Spacer(Modifier.width(8.dp))
            Surface(
              color = MaterialTheme.colorScheme.tertiaryContainer,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = stringResource(R.string.dashboard_level_format, member.nivel),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onTertiaryContainer
              )
            }
          }
        }
        val pointsToNextLevel = 100
        val currentLevelXP = member.puntos % pointsToNextLevel
        val progress = currentLevelXP.toFloat() / pointsToNextLevel.toFloat()

        if (member.tipo == TipoMiembro.PERSONA) {
            PremiumProgressBar(
                progress = progress,
                label = stringResource(R.string.family_label_xp, member.puntos),
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.tertiary
            )
        } else {
            val description = when {
              !member.raza.isNullOrBlank() -> member.raza
              !member.colorPelaje.isNullOrBlank() -> member.colorPelaje
              else -> member.tipo.name
            }
            Text(
              text = description ?: member.tipo.name,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
      }

      IconButton(onClick = onDelete) {
        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun FamilyPreview() {
  AppCasaTheme {
    FamilyContent(
      navController = NavController(androidx.compose.ui.platform.LocalContext.current),
      people = listOf(
        FamilyMember(id = 1, hogarId = 1, nombre = "Yo", tipo = TipoMiembro.PERSONA),
        FamilyMember(id = 2, hogarId = 1, nombre = "Mi mujer", tipo = TipoMiembro.PERSONA)
      ),
      pets = listOf(
        FamilyMember(id = 3, hogarId = 1, nombre = "Perro 1", tipo = TipoMiembro.PERRO),
        FamilyMember(id = 4, hogarId = 1, nombre = "Gato 1", tipo = TipoMiembro.GATO),
        FamilyMember(id = 5, hogarId = 1, nombre = "Tortuga", tipo = TipoMiembro.TORTUGA)
      ),
      onAddClick = {},
      onDeleteMember = {},
      onMemberClick = {}
    )
  }
}

