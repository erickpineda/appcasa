package com.appcasa.features.tasks.presentation.screen

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.CelebrationOverlay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.features.tasks.data.local.TareaEntity
import com.appcasa.features.tasks.presentation.viewmodel.TasksViewModel
import com.appcasa.navigation.Screen

@Composable
fun TasksScreen(
  navController: NavController,
  viewModel: TasksViewModel = hiltViewModel()
) {
  val tasks by viewModel.tasks.collectAsState()
  val isCompact by viewModel.isCompactView.collectAsState()
  val showCelebration by viewModel.showCelebration.collectAsState()
  val gainedXP by viewModel.gainedXP.collectAsState()

  if (showCelebration) {
    CelebrationOverlay(
        xp = gainedXP,
        onDismiss = { viewModel.dismissCelebration() }
    )
  }

  PullToRefreshWrapper {
    TasksContent(
      tasks = tasks,
      isCompact = isCompact,
      onAddTask = { navController.navigate(Screen.AddTask.route) },
      onToggleTask = { viewModel.toggleTaskCompletion(it) },
      onDeleteTask = { viewModel.deleteTask(it) },
      onTaskClick = { navController.navigate(Screen.TaskDetail.createRoute(it.id)) },
      onUpdateTask = { tarea, nuevoTitulo -> viewModel.updateTask(tarea, nuevoTitulo) },
      viewModel = viewModel
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksContent(
  tasks: List<TareaEntity>,
  isCompact: Boolean,
  onAddTask: () -> Unit,
  onToggleTask: (TareaEntity) -> Unit,
  onDeleteTask: (TareaEntity) -> Unit,
  onTaskClick: (TareaEntity) -> Unit,
  onUpdateTask: (TareaEntity, String) -> Unit,
  viewModel: TasksViewModel
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Tareas del Hogar") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddTask) {
        Icon(Icons.Default.Add, contentDescription = "Nueva Tarea")
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
      val pendingTasks = tasks.filter { it.estado != EstadoTarea.COMPLETADA.name }
      val completedTasks = tasks.filter { it.estado == EstadoTarea.COMPLETADA.name }

      if (pendingTasks.isEmpty() && completedTasks.isEmpty()) {
        item {
          AppCasaEmptyState(
            title = "¡Todo al día!",
            description = "No hay tareas pendientes en tu hogar. Disfruta de tu tiempo libre.",
            icon = Icons.Default.CheckCircle,
            actionText = "Crear primera tarea",
            onActionClick = onAddTask,
            modifier = Modifier.fillParentMaxSize()
          )
        }
      }

      if (pendingTasks.isNotEmpty()) {
        item {
          Text("Pendientes", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        items(pendingTasks) { tarea ->
          val subTasks by viewModel.getSubTasksCount(tarea.id).collectAsState(initial = 0 to 0)
          TaskItem(
            tarea = tarea, 
            isCompact = isCompact,
            onToggle = { onToggleTask(tarea) }, 
            onDelete = { onDeleteTask(tarea) },
            onClick = { onTaskClick(tarea) },
            onUpdate = { onUpdateTask(tarea, it) },
            subTaskInfo = subTasks
          )
        }
      }

      if (completedTasks.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 16.dp))
          Text("Completadas", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(completedTasks) { tarea ->
          val subTasks by viewModel.getSubTasksCount(tarea.id).collectAsState(initial = 0 to 0)
          TaskItem(
            tarea = tarea, 
            isCompact = isCompact,
            onToggle = { onToggleTask(tarea) }, 
            onDelete = { onDeleteTask(tarea) },
            onClick = { onTaskClick(tarea) },
            onUpdate = { onUpdateTask(tarea, it) },
            subTaskInfo = subTasks
          )
        }
      }
    }
  }
}

@Composable
fun TaskItem(
  tarea: TareaEntity,
  isCompact: Boolean,
  onToggle: () -> Unit,
  onDelete: () -> Unit,
  onClick: () -> Unit,
  onUpdate: (String) -> Unit,
  subTaskInfo: Pair<Int, Int> = 0 to 0
) {
  val isCompleted = tarea.estado == EstadoTarea.COMPLETADA.name
  var isEditing by remember { mutableStateOf(false) }
  var editedText by remember { mutableStateOf(tarea.titulo) }
  
  AppCasaCard(
    modifier = Modifier.fillMaxWidth().alpha(if (isCompleted) 0.6f else 1f),
    onClick = if (isEditing) null else onClick,
    useGlassmorphism = !isCompleted
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = if (isCompact) 4.dp else 8.dp, vertical = if (isCompact) 2.dp else 8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onToggle, enabled = !isEditing, modifier = Modifier.size(if (isCompact) 32.dp else 48.dp)) {
        Icon(
          imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
          contentDescription = null,
          tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
        )
      }
      
      if (isEditing) {
          OutlinedTextField(
              value = editedText,
              onValueChange = { editedText = it },
              modifier = Modifier.weight(1f),
              singleLine = true,
              textStyle = MaterialTheme.typography.bodyLarge,
              trailingIcon = {
                  Row {
                      IconButton(onClick = { 
                          if (editedText.isNotBlank()) {
                              onUpdate(editedText)
                              isEditing = false
                          }
                      }, modifier = Modifier.size(28.dp)) {
                          Icon(Icons.Default.Check, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                      }
                      IconButton(onClick = { 
                          editedText = tarea.titulo
                          isEditing = false 
                      }, modifier = Modifier.size(28.dp)) {
                          Icon(Icons.Default.Close, contentDescription = "Cancelar", modifier = Modifier.size(18.dp))
                      }
                  }
              }
          )
      } else {
          Column(modifier = Modifier.weight(1f).padding(vertical = if (isCompact) 4.dp else 0.dp)) {
            Text(
              text = tarea.titulo,
              style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
              modifier = Modifier.clickable(enabled = !isCompleted) { isEditing = true }
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tarea.prioridad != Prioridad.MEDIA.name && !isCompleted) {
                  Text(
                    text = tarea.prioridad,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tarea.prioridad == Prioridad.ALTA.name) MaterialTheme.colorScheme.error 
                    else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 8.dp)
                  )
                }
                
                if (subTaskInfo.first > 0) {
                    Text(
                        text = "📋 ${subTaskInfo.second}/${subTaskInfo.first}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
          }

          if (tarea.fotoUri != null && !isCompact) {
            AsyncImage(
              model = tarea.fotoUri,
              contentDescription = null,
              modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small),
              contentScale = ContentScale.Crop
            )
          }

          IconButton(onClick = onDelete, modifier = Modifier.size(if (isCompact) 32.dp else 48.dp)) {
            Icon(
                Icons.Default.Delete, 
                contentDescription = "Borrar", 
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                modifier = Modifier.size(if (isCompact) 18.dp else 24.dp)
            )
          }
      }
    }
  }
}
