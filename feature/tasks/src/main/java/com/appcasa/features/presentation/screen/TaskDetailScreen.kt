package com.appcasa.features.tasks.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.features.tasks.data.local.TareaCheckItemEntity
import com.appcasa.features.tasks.presentation.viewmodel.TaskDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val task by viewModel.task.collectAsState()
    val subTasks by viewModel.subTasks.collectAsState()
    var newSubTaskText by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    
    var selectedItems by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedItems.isNotEmpty()

    PullToRefreshWrapper {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        if (isSelectionMode) {
                            Text("${selectedItems.size} seleccionados")
                        } else {
                            Text(task?.titulo ?: "Tarea") 
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
                            val selectedSubTasks = subTasks.filter { selectedItems.contains(it.id) }
                            val allSelectedCompleted = selectedSubTasks.all { it.completado }

                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleSubTasksCompletion(selectedSubTasks, !allSelectedCompleted)
                                selectedItems = emptySet()
                            }) {
                                Icon(
                                    imageVector = if (allSelectedCompleted) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                                    contentDescription = "Cambiar estado"
                                )
                            }

                            IconButton(onClick = {
                                if (selectedItems.size == subTasks.size) {
                                    selectedItems = emptySet()
                                } else {
                                    selectedItems = subTasks.map { it.id }.toSet()
                                }
                            }) {
                                Icon(
                                    imageVector = if (selectedItems.size == subTasks.size) Icons.Default.Deselect else Icons.Default.SelectAll,
                                    contentDescription = "Seleccionar todo"
                                )
                            }
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val toDelete = subTasks.filter { selectedItems.contains(it.id) }
                                viewModel.deleteSubTasks(toDelete)
                                selectedItems = emptySet()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Borrar seleccionados", tint = MaterialTheme.colorScheme.error)
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
                task?.let { currentTask ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            AnimatedVisibility(visible = !isSelectionMode) {
                                currentTask.fotoUri?.let { uri ->
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Foto",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .padding(16.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        item {
                            if (!currentTask.descripcion.isNullOrBlank() && !isSelectionMode) {
                                Text(
                                    text = currentTask.descripcion ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Pasos de la tarea",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                if (!isSelectionMode && subTasks.isNotEmpty()) {
                                    TextButton(
                                        onClick = { selectedItems = subTasks.map { it.id }.toSet() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.LibraryAddCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Seleccionar", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        item {
                            AnimatedVisibility(visible = !isSelectionMode) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newSubTaskText,
                                        onValueChange = { newSubTaskText = it },
                                        placeholder = { Text("Añadir paso...") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                    )
                                    FloatingActionButton(
                                        onClick = {
                                            if (newSubTaskText.isNotBlank()) {
                                                viewModel.addSubTask(newSubTaskText)
                                                newSubTaskText = ""
                                            }
                                        },
                                        modifier = Modifier.size(40.dp),
                                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Añadir")
                                    }
                                }
                            }
                        }

                        items(subTasks, key = { it.id }) { item ->
                            val isSelected = selectedItems.contains(item.id)
                            CompactSubTaskItemEditable(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onToggleSelection = {
                                    selectedItems = if (isSelected) selectedItems - item.id else selectedItems + item.id
                                },
                                onToggle = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleSubTask(item) 
                                },
                                onDelete = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.deleteSubTask(item) 
                                },
                                onEdit = { nuevoTexto -> viewModel.updateSubTask(item, nuevoTexto) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun CompactSubTaskItemEditable(
    item: TareaCheckItemEntity,
    isSelected: Boolean,
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
                modifier = Modifier.padding(horizontal = 4.dp).scale(0.8f)
            )
        } else {
            IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (item.completado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (item.completado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
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
                        IconButton(onClick = { if (editedText.isNotBlank()) { onEdit(editedText); isEditing = false } }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Check, contentDescription = "OK", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { editedText = item.texto; isEditing = false }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "X", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            )
        } else {
            Text(
                text = item.texto,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (item.completado) TextDecoration.LineThrough else null,
                color = if (item.completado) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !isSelectionMode) { isEditing = true }
                    .padding(vertical = 4.dp)
            )

            if (!isSelectionMode) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Borrar", 
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Extensión para escalar componentes si es necesario
@Composable
fun Modifier.scale(scale: Float): Modifier = this.then(
    androidx.compose.ui.draw.scale(scale)
)
