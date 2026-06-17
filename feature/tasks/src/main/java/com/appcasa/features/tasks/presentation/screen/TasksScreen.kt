package com.appcasa.features.tasks.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.Task
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.AppCasaSutilToast
import com.appcasa.core.ui.components.CelebrationOverlay
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.components.SyncStatusBadge
import com.appcasa.feature.tasks.R
import com.appcasa.features.tasks.presentation.viewmodel.TasksViewModel
import com.appcasa.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun TasksScreen(
  navController: NavController,
  viewModel: TasksViewModel = hiltViewModel()
) {
  val tasks by viewModel.tasks.collectAsStateWithLifecycle()
  val members by viewModel.familyMembers.collectAsStateWithLifecycle()
  val isCompact by viewModel.isCompactView.collectAsStateWithLifecycle()
  val showCelebration by viewModel.showCelebration.collectAsStateWithLifecycle()
  val gainedXP by viewModel.gainedXP.collectAsStateWithLifecycle()
  val subTaskCounts by viewModel.subTaskCounts.collectAsStateWithLifecycle()
  var toastMessage by remember { mutableStateOf<String?>(null) }
  var taskToArchive by remember { mutableStateOf<Task?>(null) }
  
  val memberMap = remember(members) { members.associate { it.id to it.nombre } }

  LaunchedEffect(Unit) {
    viewModel.toastEvent.collect { message ->
        toastMessage = message
    }
  }

  AppCasaConfirmDialog(
    show = taskToArchive != null,
    title = stringResource(R.string.task_delete),
    text = stringResource(R.string.task_archive_confirm),
    onConfirm = {
        taskToArchive?.let { viewModel.archiveTask(it) }
        taskToArchive = null
    },
    onDismiss = { taskToArchive = null }
  )

  Box(modifier = Modifier.fillMaxSize()) {
      PullToRefreshWrapper {
        TasksContent(
          tasks = tasks,
          isCompact = isCompact,
          subTaskCounts = subTaskCounts,
          memberMap = memberMap,
          onAddTask = { navController.navigate(Screen.AddTask) },
          onToggleTask = { viewModel.toggleTaskCompletion(it) },
          onDeleteTask = { taskToArchive = it },
          onTaskClick = { navController.navigate(Screen.TaskDetail(it.id)) },
          onUpdateTask = { tarea, nuevoTitulo -> viewModel.updateTask(tarea, nuevoTitulo) },
          onLoadMore = { viewModel.loadMoreActive() }
        )
      }

      AppCasaSutilToast(
          message = toastMessage,
          onDismiss = { toastMessage = null }
      )

      if (showCelebration) {
        CelebrationOverlay(
            xp = gainedXP,
            onDismiss = { viewModel.dismissCelebration() }
        )
      }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksContent(
  tasks: List<Task>,
  isCompact: Boolean,
  subTaskCounts: Map<Long, Pair<Int, Int>>,
  memberMap: Map<Long, String>,
  onAddTask: () -> Unit,
  onToggleTask: (Task) -> Unit,
  onDeleteTask: (Task) -> Unit,
  onTaskClick: (Task) -> Unit,
  onUpdateTask: (Task, String) -> Unit,
  onLoadMore: () -> Unit
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
      val pendingTasks = tasks.filter { it.estado != EstadoTarea.COMPLETADA }
      val completedTasks = tasks.filter { it.estado == EstadoTarea.COMPLETADA }

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
            subTaskInfo = subTaskCounts[tarea.id] ?: (0 to 0),
            creatorName = tarea.createdById?.let { memberMap[it] }
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
            subTaskInfo = subTaskCounts[tarea.id] ?: (0 to 0),
            creatorName = tarea.createdById?.let { memberMap[it] }
          )
        }
      }

      if (tasks.isNotEmpty()) {
        item {
          TextButton(
            onClick = onLoadMore,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(stringResource(R.string.task_load_more))
          }
        }
      }
    }
  }
}

@Composable
fun TaskItem(
  tarea: Task,
  isCompact: Boolean,
  onToggle: () -> Unit,
  onDelete: () -> Unit,
  onClick: () -> Unit,
  onUpdate: (String) -> Unit,
  subTaskInfo: Pair<Int, Int> = 0 to 0,
  creatorName: String? = null
) {
  val isCompleted = tarea.estado == EstadoTarea.COMPLETADA
  var isEditing by remember { mutableStateOf(false) }
  var editedText by remember { mutableStateOf(tarea.titulo) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(isEditing) {
      if (isEditing) {
          delay(50)
          focusRequester.requestFocus()
          keyboardController?.show()
      }
  }
  
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
              modifier = Modifier.weight(1f).focusRequester(focusRequester),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = tarea.titulo,
                style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                modifier = Modifier.clickable(enabled = !isCompleted) { isEditing = true }
              )
              Spacer(Modifier.width(6.dp))
              SyncStatusBadge(isSynced = tarea.lastSyncedAt != null && tarea.lastSyncedAt!! >= tarea.updatedAt)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tarea.prioridad != Prioridad.MEDIA && !isCompleted) {
                  Text(
                    text = tarea.prioridad.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tarea.prioridad == Prioridad.ALTA) MaterialTheme.colorScheme.error
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

                if (creatorName != null && !isCompact) {
                  Text(
                    text = " • $creatorName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
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

