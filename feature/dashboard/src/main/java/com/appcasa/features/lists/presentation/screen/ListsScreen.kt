package com.appcasa.features.lists.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.TipoLista
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.lists.presentation.viewmodel.ListsViewModel
import com.appcasa.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
  navController: NavController,
  viewModel: ListsViewModel = hiltViewModel()
) {
  val lists by viewModel.lists.collectAsState()
  val isCompact by viewModel.isCompactView.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }

  if (showAddDialog) {
    AddListDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { name, type ->
        viewModel.addList(name, type)
        showAddDialog = false
      }
    )
  }

  PullToRefreshWrapper {
    ListsContent(
      lists = lists,
      isCompact = isCompact,
      onListClick = { listId ->
        navController.navigate(Screen.ListDetail.createRoute(listId))
      },
      onDeleteList = { viewModel.deleteList(it) },
      onAddClick = { showAddDialog = true },
      onUpdateList = { lista, nuevoNombre -> viewModel.updateList(lista, nuevoNombre) }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsContent(
  lists: List<ListaEntity>,
  isCompact: Boolean,
  onListClick: (Long) -> Unit,
  onDeleteList: (ListaEntity) -> Unit,
  onAddClick: () -> Unit,
  onUpdateList: (ListaEntity, String) -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Mis Listas") },
        navigationIcon = {
          IconButton(onClick = { /* Ir atrás si fuera necesario, pero este suele ser un Hub */ }) {
            // Si quieres navegación atrás, añádela aquí. Generalmente en Hubs no hay flecha si es pestaña principal.
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary,
          navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddClick) {
        Icon(Icons.Default.Add, contentDescription = "Nueva Lista")
      }
    }
  ) { scaffoldPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(scaffoldPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 8.dp)
    ) {
      if (lists.isEmpty()) {
        item {
          AppCasaEmptyState(
            title = "Sin listas",
            description = "Crea listas de la compra, tareas pendientes o lo que necesites organizar.",
            icon = Icons.AutoMirrored.Filled.List,
            actionText = "Crear lista",
            onActionClick = onAddClick,
            modifier = Modifier.fillParentMaxSize()
          )
        }
      } else {
        items(lists) { lista ->
          CompactListCard(
            lista = lista, 
            isCompact = isCompact,
            onClick = { onListClick(lista.id) },
            onDelete = { onDeleteList(lista) },
            onUpdate = { onUpdateList(lista, it) }
          )
        }
      }
    }
  }
}

@Composable
fun CompactListCard(
  lista: ListaEntity, 
  isCompact: Boolean,
  onClick: () -> Unit, 
  onDelete: () -> Unit,
  onUpdate: (String) -> Unit
) {
  var isEditing by remember { mutableStateOf(false) }
  var editedText by remember { mutableStateOf(lista.nombre) }

  val icon = when (lista.tipo) {
    TipoLista.COMPRA.name -> Icons.Default.ShoppingCart
    TipoLista.FARMACIA.name -> Icons.Default.MedicalServices
    TipoLista.VETERINARIO.name -> Icons.Default.Pets
    TipoLista.VIAJE.name -> Icons.Default.Flight
    TipoLista.ESCOLAR.name -> Icons.Default.School
    else -> Icons.AutoMirrored.Filled.List
  }

  com.appcasa.core.ui.components.AppCasaCard(
    useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth(),
    onClick = if (isEditing) null else onClick
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = if (isCompact) 4.dp else 8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
    ) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(if (isCompact) 32.dp else 40.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(if (isCompact) 16.dp else 20.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }
      
      if (isEditing) {
        OutlinedTextField(
          value = editedText,
          onValueChange = { editedText = it },
          modifier = Modifier.weight(1f),
          singleLine = true,
          textStyle = MaterialTheme.typography.titleMedium,
          trailingIcon = {
            Row {
              IconButton(onClick = { 
                if (editedText.isNotBlank()) {
                  onUpdate(editedText)
                  isEditing = false
                }
              }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
              }
              IconButton(onClick = { 
                editedText = lista.nombre
                isEditing = false 
              }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
              }
            }
          }
        )
      } else {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = lista.nombre, 
            style = if (isCompact) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { isEditing = true }
          )
          if (!isCompact) {
            Text(
              text = lista.tipo, 
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(if (isCompact) 32.dp else 48.dp)) {
          Icon(
            Icons.Default.Delete, 
            contentDescription = "Borrar", 
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
            modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun AddListDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
  var name by remember { mutableStateOf("") }
  var nameTouched by remember { mutableStateOf(false) }

  val canConfirm = name.isNotBlank()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Nueva Lista") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name, 
            onValueChange = { 
                name = it
                nameTouched = true
            }, 
            label = { Text("Nombre de la lista") }, 
            modifier = Modifier.fillMaxWidth(),
            isError = nameTouched && name.isBlank(),
            supportingText = {
                if (nameTouched && name.isBlank()) {
                    Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error)
                }
            }
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name, "PERSONALIZADA") },
        enabled = canConfirm
      ) { Text("Crear") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ListsPreview() {
  AppCasaTheme {
    ListsContent(
      lists = listOf(
        ListaEntity(id = 1, hogarId = 1, nombre = "Compra Mercadona", tipo = TipoLista.COMPRA.name),
        ListaEntity(id = 2, hogarId = 1, nombre = "Botiquín Verano", tipo = TipoLista.FARMACIA.name)
      ),
      isCompact = false,
      onListClick = {},
      onDeleteList = {},
      onAddClick = {},
      onUpdateList = { _, _ -> }
    )
  }
}
