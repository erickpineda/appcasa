package com.appcasa.features.family.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.family.presentation.viewmodel.FamilyViewModel
import com.appcasa.navigation.Screen

import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun FamilyScreen(
  navController: NavController,
  viewModel: FamilyViewModel = hiltViewModel()
) {
  val people by viewModel.people.collectAsState()
  val pets by viewModel.pets.collectAsState()

  PullToRefreshWrapper {
    FamilyContent(
      people = people,
      pets = pets,
      onAddClick = { navController.navigate(Screen.AddMember.route) },
      onDeleteMember = { viewModel.deleteMember(it) },
      onMemberClick = { member ->
        if (member.tipo == TipoMiembro.PERSONA.name) {
          navController.navigate(Screen.MemberDetail.createRoute(member.id))
        } else {
          navController.navigate(Screen.PetDetail.createRoute(member.id))
        }
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyContent(
  people: List<MiembroEntity>,
  pets: List<MiembroEntity>,
  onAddClick: () -> Unit,
  onDeleteMember: (MiembroEntity) -> Unit,
  onMemberClick: (MiembroEntity) -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Familia y Mascotas") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddClick) {
        Icon(Icons.Default.Add, contentDescription = "Añadir")
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
          Column(
            modifier = Modifier.fillParentMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Groups,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              "Aún no has añadido a tu familia o mascotas",
              style = MaterialTheme.typography.bodyLarge
            )
          }
        }
      }

      if (people.isNotEmpty()) {
        item {
          Text(
            text = "Miembros de la Familia",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
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
          Text(
            text = "Nuestras Mascotas",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
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
  member: MiembroEntity,
  icon: ImageVector,
  onDelete: () -> Unit,
  onClick: () -> Unit
) {
  com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth().alpha(0.8f),
    onClick = onClick
  ) {
    Row(
      modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
      ) {
        Box(contentAlignment = Alignment.Center) {
          if (member.fotoUri != null) {
            AsyncImage(
              model = member.fotoUri,
              contentDescription = null,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          } else {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = member.nombre,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = if (member.tipo == TipoMiembro.PERSONA.name) "Humano" else member.tipo,
          style = MaterialTheme.typography.bodySmall
        )
      }

      IconButton(onClick = onDelete) {
        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun FamilyPreview() {
  AppCasaTheme {
    FamilyContent(
      people = listOf(
        MiembroEntity(id = 1, hogarId = 1, nombre = "Yo", tipo = TipoMiembro.PERSONA.name),
        MiembroEntity(id = 2, hogarId = 1, nombre = "Mi mujer", tipo = TipoMiembro.PERSONA.name)
      ),
      pets = listOf(
        MiembroEntity(id = 3, hogarId = 1, nombre = "Perro 1", tipo = TipoMiembro.PERRO.name),
        MiembroEntity(id = 4, hogarId = 1, nombre = "Gato 1", tipo = TipoMiembro.GATO.name),
        MiembroEntity(id = 5, hogarId = 1, nombre = "Tortuga", tipo = TipoMiembro.TORTUGA.name)
      ),
      onAddClick = {},
      onDeleteMember = {},
      onMemberClick = {}
    )
  }
}
