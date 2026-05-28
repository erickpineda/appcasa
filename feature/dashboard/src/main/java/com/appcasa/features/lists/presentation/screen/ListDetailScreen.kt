package com.appcasa.features.lists.presentation.screen

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.features.lists.data.local.ListaItemEntity
import com.appcasa.features.lists.presentation.viewmodel.ListDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
  navController: NavController,
  viewModel: ListDetailViewModel = hiltViewModel()
) {
  val items by viewModel.items.collectAsState()
  val isCompact by viewModel.isCompactView.collectAsState()
  var newItemText by remember { mutableStateOf("") }
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  
  var selectedItems by remember { mutableStateOf(setOf<Long>()) }
  val isSelectionMode = selectedItems.isNotEmpty()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { 
          if (isSelectionMode) {
            Text("${selectedItems.size} seleccionados")
          } else {
            Text("Items de la Lista")
          }
        },
        navigationIcon = {
          if (isSelectionMode) {
            IconButton(onClick = { selectedItems = emptySet() }) {
              Icon(Icons.Default.Close, contentDescription = "Cancelar selección")
            }
          } else {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
          }
        },
        actions = {
          if (isSelectionMode) {
            val selectedListItems = items.filter { selectedItems.contains(it.id) }
            val allSelectedCompleted = selectedListItems.all { it.completado }

            IconButton(onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.toggleItemsCompletion(selectedListItems, !allSelectedCompleted)
              selectedItems = emptySet()
            }) {
              Icon(
                imageVector = if (allSelectedCompleted) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                contentDescription = "Cambiar estado"
              )
            }

            IconButton(onClick = {
              if (selectedItems.size == items.size) {
                selectedItems = emptySet()
              } else {
                selectedItems = items.map { it.id }.toSet()
              }
            }) {
              Icon(
                imageVector = if (selectedItems.size == items.size) Icons.Default.Deselect else Icons.Default.SelectAll,
                contentDescription = "Seleccionar todo"
              )
            }
            IconButton(onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              val toDelete = items.filter { selectedItems.contains(it.id) }
              viewModel.deleteItems(toDelete)
              selectedItems = emptySet()
            }) {
              Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
            }
          } else {
            IconButton(onClick = {
              val shareText = "🛒 *Lista de AppCasa*:\n" + items.joinToString("\n") { 
                (if (it.completado) "✅ " else "⬜ ") + it.texto 
              }
              val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
              }
              context.startActivity(Intent.createChooser(sendIntent, null))
            }) {
              Icon(Icons.Default.Share, contentDescription = "Compartir")
            }
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
    ) {
      if (!isSelectionMode) {
        Row(
          modifier = Modifier
            .padding(horizontal = 16.dp, vertical = if (isCompact) 2.dp else 4.dp)
            .fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = newItemText,
            onValueChange = { newItemText = it },
            placeholder = { Text("Nuevo ítem...") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
          )
          FloatingActionButton(
            onClick = {
              if (newItemText.isNotBlank()) {
                viewModel.addItem(newItemText)
                newItemText = ""
              }
            },
            modifier = Modifier.size(if (isCompact) 40.dp else 48.dp),
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir")
          }
        }
      }

      HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )
      
      if (!isSelectionMode && items.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = if (isCompact) 0.dp else 2.dp),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = { selectedItems = items.map { it.id }.toSet() },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(if (isCompact) 28.dp else 32.dp)
          ) {
            Icon(Icons.Default.LibraryAddCheck, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Seleccionar", style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium)
          }
        }
      }

      LazyColumn(
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
      ) {
        items(items, key = { it.id }) { item ->
          val isSelected = selectedItems.contains(item.id)
          CompactListItemEditable(
            item = item,
            isSelected = isSelected,
            isCompact = isCompact,
            isSelectionMode = isSelectionMode,
            onToggleSelection = {
              selectedItems = if (isSelected) selectedItems - item.id else selectedItems + item.id
            },
            onToggle = { 
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.toggleItemCompletion(item) 
            },
            onDelete = { 
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              viewModel.deleteItem(item) 
            },
            onEdit = { nuevoTexto -> viewModel.updateItem(item, nuevoTexto) }
          )
          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp), 
            thickness = 0.5.dp, 
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)
          )
        }
      }
    }
  }
}

@Composable
fun CompactListItemEditable(
  item: ListaItemEntity,
  isSelected: Boolean,
  isCompact: Boolean,
  isSelectionMode: Boolean,
  onToggleSelection: () -> Unit,
  onToggle: () -> Unit,
  onDelete: () -> Unit,
  onEdit: (String) -> Unit
) {
  var isEditing by remember { mutableStateOf(false) }
  var editedText by remember { mutableStateOf(item.texto) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { if (isSelectionMode) onToggleSelection() else onToggle() }
      .padding(horizontal = 4.dp, vertical = 0.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (isSelectionMode) {
      Checkbox(
        checked = isSelected,
        onCheckedChange = { onToggleSelection() },
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    } else {
      IconButton(onClick = onToggle, modifier = Modifier.size(if (isCompact) 32.dp else 40.dp)) {
        Icon(
          imageVector = if (item.completado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
          contentDescription = null,
          tint = if (item.completado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
        )
      }
    }
    
    if (isEditing && !isSelectionMode) {
      OutlinedTextField(
        value = editedText,
        onValueChange = { editedText = it },
        modifier = Modifier.weight(1f).padding(vertical = 2.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        trailingIcon = {
          Row {
            IconButton(onClick = { if (editedText.isNotBlank()) { onEdit(editedText); isEditing = false } }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Check, contentDescription = "OK", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { editedText = item.texto; isEditing = false }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Close, contentDescription = "X", modifier = Modifier.size(16.dp))
            }
          }
        }
      )
    } else {
      Text(
        text = item.texto,
        style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
        textDecoration = if (item.completado) TextDecoration.LineThrough else null,
        color = if (item.completado) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
          .weight(1f)
          .clickable(enabled = !isSelectionMode) { isEditing = true }
          .padding(vertical = if (isCompact) 4.dp else 8.dp)
      )

      if (!isSelectionMode) {
        IconButton(onClick = onDelete, modifier = Modifier.size(if (isCompact) 32.dp else 40.dp)) {
          Icon(
            Icons.Default.Delete, 
            contentDescription = "Borrar", 
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
            modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
          )
        }
      }
    }
  }
}
