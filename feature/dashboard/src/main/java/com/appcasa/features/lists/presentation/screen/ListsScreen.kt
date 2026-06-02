package com.appcasa.features.lists.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.TipoLista
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.AppCasaSutilToast
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.feature.dashboard.R
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
  var toastMessage by remember { mutableStateOf<String?>(null) }
  var listToArchive by remember { mutableStateOf<ListaEntity?>(null) }

  LaunchedEffect(Unit) {
    viewModel.toastEvent.collect { message ->
        toastMessage = message
    }
  }

  AppCasaConfirmDialog(
    show = listToArchive != null,
    title = stringResource(R.string.cd_delete),
    text = "Se moverá esta lista al Cajón de Archivo. Podrás recuperarla desde allí si la necesitas.",
    onConfirm = {
        listToArchive?.let { viewModel.archiveList(it) }
        listToArchive = null
    },
    onDismiss = { listToArchive = null }
  )

  if (showAddDialog) {
    AddListDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { name, type ->
        viewModel.addList(name, type)
        showAddDialog = false
      }
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
      PullToRefreshWrapper {
        ListsContent(
          lists = lists,
          isCompact = isCompact,
          onListClick = { listId ->
            navController.navigate(Screen.ListDetail.createRoute(listId))
          },
          onDeleteList = { listToArchive = it },
          onAddClick = { showAddDialog = true },
          onUpdateList = { lista, nuevoNombre -> viewModel.updateList(lista, nuevoNombre) },
          onLoadMore = { viewModel.loadMoreActive() }
        )
      }

      AppCasaSutilToast(
          message = toastMessage,
          onDismiss = { toastMessage = null }
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
  onUpdateList: (ListaEntity, String) -> Unit,
  onLoadMore: () -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.lists_title)) },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary,
          navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddClick) {
        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.lists_new_title))
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
            title = stringResource(R.string.dashboard_no_lists_title),
            description = stringResource(R.string.dashboard_no_lists_desc),
            icon = Icons.AutoMirrored.Filled.List,
            actionText = stringResource(R.string.dashboard_btn_create_list),
            onActionClick = onAddClick,
            modifier = Modifier.fillParentMaxSize()
          )
        }
      } else {
        items(lists, key = { it.id }) { lista ->
          CompactListCard(
            lista = lista, 
            isCompact = isCompact,
            onClick = { onListClick(lista.id) },
            onDelete = { onDeleteList(lista) },
            onUpdate = { onUpdateList(lista, it) }
          )
        }
        
        item {
          TextButton(
            onClick = onLoadMore, 
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Cargar más listas antiguas...")
          }
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

  AppCasaCard(
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
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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
            contentDescription = stringResource(R.string.cd_delete), 
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
    title = { Text(stringResource(R.string.lists_new_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name, 
            onValueChange = { 
                name = it
                nameTouched = true
            }, 
            label = { Text(stringResource(R.string.lists_label_name)) }, 
            modifier = Modifier.fillMaxWidth(),
            isError = nameTouched && name.isBlank(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            supportingText = {
                if (nameTouched && name.isBlank()) {
                    Text(stringResource(R.string.family_error_name_required), color = MaterialTheme.colorScheme.error)
                }
            }
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name, "PERSONALIZADA") },
        enabled = canConfirm
      ) { Text(stringResource(R.string.lists_btn_create)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.lists_btn_cancel)) }
    }
  )
}
