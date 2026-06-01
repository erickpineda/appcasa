package com.appcasa.features.tasks.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.CelebrationOverlay
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.feature.tasks.R
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
  val subTaskCounts by viewModel.subTaskCounts.collectAsState()

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
      subTaskCounts = subTaskCounts,
      onAddTask = { navController.navigate(Screen.AddTask.route) },
      onToggleTask = { viewModel.toggleTaskCompletion(it) },
      onDeleteTask = { viewModel.deleteTask(it) },
      onTaskClick = { navController.navigate(Screen.TaskDetail.createRoute(it.id)) },
      onUpdateTask = { tarea, nuevoTitulo -> viewModel.updateTask(tarea, nuevoTitulo) }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksContent(
  tasks: List<TareaEntity>,
  isCompact: Boolean,
  subTaskCounts: Map<Long, Pair<Int, Int>>,
  onAddTask: () -> Unit,
  onToggleTask: (TareaEntity) -> Unit,
  onDeleteTask: (TareaEntity) -> Unit,
  onTaskClick: (TareaEntity) -> Unit,
  onUpdateTask: (TareaEntity, String) -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.tasks_title)) },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddTask) {
        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.tasks_new_task))
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
            title = stringResource(R.string.tasks_empty_title),
            description = stringResource(R.string.tasks_empty_description),
            icon = Icons.Default.CheckCircle,
            actionText = stringResource(R.string.tasks_create_first),
            onActionClick = onAddTask,
            modifier = Modifier.fillParentMaxSize()
          )
        }
      }

      if (pendingTasks.isNotEmpty()) {
        item {
          Text(stringResource(R.string.tasks_pending), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        items(pendingTasks, key = { it.id }) { tarea ->
          TaskItem(
            tarea = tarea, 
            isCompact = isCompact,
            onToggle = { onToggleTask(tarea) }, 
            onDelete = { onDeleteTask(tarea) },
            onClick = { onTaskClick(tarea) },
            onUpdate = { onUpdateTask(tarea, it) },
            subTaskInfo = subTaskCounts[tarea.id] ?: (0 to 0)
          )
        }
      }

      if (completedTasks.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 16.dp))
          Text(stringResource(R.string.tasks_completed), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(completedTasks, key = { it.id }) { tarea ->
          TaskItem(
            tarea = tarea, 
            isCompact = isCompact,
            onToggle = { onToggleTask(tarea) }, 
            onDelete = { onDeleteTask(tarea) },
            onClick = { onTaskClick(tarea) },
            onUpdate = { onUpdateTask(tarea, it) },
            subTaskInfo = subTaskCounts[tarea.id] ?: (0 to 0)
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
                          Icon(Icons.Default.Check, contentDescription = stringResource(R.string.task_save), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                      }
                      IconButton(onClick = { 
                          editedText = tarea.titulo
                          isEditing = false 
                      }, modifier = Modifier.size(28.dp)) {
                          Icon(Icons.Default.Close, contentDescription = stringResource(R.string.task_cancel), modifier = Modifier.size(18.dp))
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
                        text = stringResource(R.string.task_subtasks_format, subTaskInfo.second, subTaskInfo.first),
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
                contentDescription = stringResource(R.string.task_delete),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                modifier = Modifier.size(if (isCompact) 18.dp else 24.dp)
            )
          }
      }
    }
  }
}
