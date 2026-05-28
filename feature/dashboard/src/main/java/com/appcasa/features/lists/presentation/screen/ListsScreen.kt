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
  onListClick: (Long) -> Unit,
  onDeleteList: (ListaEntity) -> Unit,
  onAddClick: () -> Unit,
  onUpdateList: (ListaEntity, String) -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Mis Listas") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
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
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (lists.isEmpty()) {
        item {
          Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tienes listas creadas", style = MaterialTheme.typography.bodyLarge)
          }
        }
      } else {
        items(lists) { lista ->
          CompactListCard(
            lista = lista, 
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
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(40.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
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
                      }) {
                          Icon(Icons.Default.Check, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary)
                      }
                      IconButton(onClick = { 
                          editedText = lista.nombre
                          isEditing = false 
                      }) {
                          Icon(Icons.Default.Close, contentDescription = "Cancelar")
                      }
                  }
              }
          )
      } else {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = lista.nombre, 
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.clickable { isEditing = true }
            )
            Text(
              text = lista.tipo, 
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary
            )
          }

          IconButton(onClick = onDelete) {
            Icon(
              Icons.Default.Delete, 
              contentDescription = "Borrar", 
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
              modifier = Modifier.size(20.dp)
            )
          }
      }
    }
  }
}

@Composable
fun AddListDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Lista") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre de la lista") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, "PERSONALIZADA") }) { Text("Crear") }
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
      onListClick = {},
      onDeleteList = {},
      onAddClick = {},
      onUpdateList = { _, _ -> }
    )
  }
}
