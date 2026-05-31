package com.appcasa.features.family.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.family.presentation.viewmodel.FamilyViewModel
import com.appcasa.navigation.Screen

import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun FamilyScreen(
  navController: NavController,
  viewModel: FamilyViewModel = hiltViewModel()
) {
  val people by viewModel.people.collectAsState()
  val pets by viewModel.pets.collectAsState()
  
  var memberToDelete by remember { mutableStateOf<MiembroEntity?>(null) }

  AppCasaConfirmDialog(
    show = memberToDelete != null,
    title = "Eliminar Miembro",
    text = "¿Estás seguro de que quieres eliminar a ${memberToDelete?.nombre}? Se perderá todo su progreso y XP.",
    onConfirm = {
        memberToDelete?.let { viewModel.deleteMember(it) }
        memberToDelete = null
    },
    onDismiss = { memberToDelete = null }
  )

  PullToRefreshWrapper {
    FamilyContent(
      navController = navController,
      people = people,
      pets = pets,
      onAddClick = { navController.navigate(Screen.AddMember.route) },
      onDeleteMember = { memberToDelete = it },
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
  navController: NavController,
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
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
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
          AppCasaEmptyState(
            title = "Tu hogar está vacío",
            description = "Añade a los miembros de tu familia y a tus mascotas para organizar mejor el día a día.",
            icon = Icons.Default.Groups,
            actionText = "Añadir primer miembro",
            onActionClick = onAddClick,
            modifier = Modifier.fillParentMaxSize()
          )
        }
      }

      if (people.isNotEmpty()) {
        item {
          Text(
            text = "Miembros de la Familia",
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
            text = "Nuestras Mascotas (${pets.size})",
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
  member: MiembroEntity,
  icon: ImageVector,
  onDelete: () -> Unit,
  onClick: () -> Unit
) {
  com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true,
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
          if (member.tipo == TipoMiembro.PERSONA.name) {
            Spacer(Modifier.width(8.dp))
            Surface(
              color = MaterialTheme.colorScheme.tertiaryContainer,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "Nivel ${member.nivel}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onTertiaryContainer
              )
            }
          }
        }
        val description = when {
          member.tipo == TipoMiembro.PERSONA.name -> "XP: ${member.puntos}"
          !member.raza.isNullOrBlank() -> member.raza
          !member.colorPelaje.isNullOrBlank() -> member.colorPelaje
          else -> member.tipo
        }
        Text(
          text = description ?: member.tipo,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
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
      navController = NavController(androidx.compose.ui.platform.LocalContext.current),
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
