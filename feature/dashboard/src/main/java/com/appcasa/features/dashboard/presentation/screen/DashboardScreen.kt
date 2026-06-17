package com.appcasa.features.dashboard.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.NextEventSummary
import com.appcasa.core.domain.model.PetSummary
import com.appcasa.core.domain.model.PostIt
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.model.User
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PremiumProgressBar
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.components.SyncStatusBadge
import com.appcasa.core.utils.Constants
import com.appcasa.feature.dashboard.R
import com.appcasa.features.dashboard.presentation.model.SearchItem
import com.appcasa.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.appcasa.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  navController: NavController,
  viewModel: DashboardViewModel = hiltViewModel()
) {
  val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val petData by viewModel.petData.collectAsStateWithLifecycle()
  val pendingTasks by viewModel.pendingTasksCount.collectAsStateWithLifecycle()
  val monthlyExpense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
  val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()
  val nextEvent by viewModel.nextEventData.collectAsStateWithLifecycle()
  val postIts by viewModel.postIts.collectAsStateWithLifecycle()
  val dashboardOrder by viewModel.dashboardOrder.collectAsStateWithLifecycle()
  val fullDashboardConfig by viewModel.fullDashboardConfig.collectAsStateWithLifecycle()
  val quickActions by viewModel.quickActions.collectAsStateWithLifecycle()
  val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val isReady by viewModel.isReady.collectAsStateWithLifecycle()
  val userPoints by viewModel.userPoints.collectAsStateWithLifecycle()
  val userLevel by viewModel.userLevel.collectAsStateWithLifecycle()

  BackHandler(enabled = searchQuery.isNotEmpty()) {
      viewModel.onSearchQueryChange("")
  }

  var showPostItDialog by remember { mutableStateOf(false) }
  var showMoodSelector by remember { mutableStateOf<FamilyMember?>(null) }
  var showReorderDialog by remember { mutableStateOf(false) }
  var editingPostIt by remember { mutableStateOf<PostIt?>(null) }

  if (showPostItDialog) {
    AddPostItDialog(
        onDismiss = { showPostItDialog = false },
        onConfirm = { content ->
            viewModel.addPostIt(content)
            showPostItDialog = false
        }
    )
  }

  if (editingPostIt != null) {
      AddPostItDialog(
          initialContent = editingPostIt!!.contenido,
          onDismiss = { editingPostIt = null },
          onConfirm = { content ->
              viewModel.updatePostIt(editingPostIt!!, content)
              editingPostIt = null
          }
      )
  }

  if (showMoodSelector != null) {
      MoodSelectorDialog(
          member = showMoodSelector!!,
          onDismiss = { showMoodSelector = null },
          onSelect = { emoji ->
              viewModel.updateMemberMood(showMoodSelector!!.id, emoji)
              showMoodSelector = null
          }
      )
  }

  if (showReorderDialog) {
      DashboardCustomizerDialog(
          currentOrder = fullDashboardConfig,
          currentQuickActions = quickActions,
          onDismiss = { showReorderDialog = false },
          onSaveOrder = { viewModel.updateDashboardOrder(it) },
          onSaveQuickActions = { viewModel.updateQuickActions(it) }
      )
  }

  if (!isReady) {
      AppCasaMeshBackground { }
  } else {
      Scaffold(
        topBar = {
          DashboardTopBar(
            user = currentUser,
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
            onSettingsClick = { navController.navigate(Screen.Settings) },
            onCustomizeClick = { showReorderDialog = true }
          )
        }
      ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
          PullToRefreshWrapper {
            DashboardContent(
              dashboardOrder = dashboardOrder,
              quickActions = quickActions,
              navController = navController,
              familyMembers = familyMembers,
              petData = petData,
              pendingTasksCount = pendingTasks,
              monthlyExpense = monthlyExpense,
              lowStockCount = lowStockCount,
              nextEvent = nextEvent,
              postIts = postIts,
              userPoints = userPoints,
              userLevel = userLevel,
              onMoodClick = { showMoodSelector = it },
              onAddPostIt = { showPostItDialog = true },
              onEditPostIt = { editingPostIt = it },
              onDeletePostIt = { viewModel.deletePostIt(it) }
            )
          }

          if (searchQuery.isNotEmpty()) {
            SearchResultsOverlay(
              results = searchResults,
              onResultClick = { item ->
                navController.navigate(item.route)
                viewModel.onSearchQueryChange("")
              },
              onClose = { viewModel.onSearchQueryChange("") }
            )
          }
        }
      }
  }
}

@Composable
fun DashboardContent(
  dashboardOrder: List<String>,
  quickActions: List<String>,
  navController: NavController,
  familyMembers: List<FamilyMember>,
  petData: PetSummary,
  pendingTasksCount: String,
  monthlyExpense: String,
  lowStockCount: Int,
  nextEvent: NextEventSummary?,
  postIts: List<PostIt>,
  userPoints: Int,
  userLevel: Int,
  onMoodClick: (FamilyMember) -> Unit,
  onAddPostIt: () -> Unit,
  onEditPostIt: (PostIt) -> Unit,
  onDeletePostIt: (PostIt) -> Unit
) {
  val listState = rememberLazyListState()

  LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
        FamilyStatusRow(
            members = familyMembers,
            onMoodClick = onMoodClick
        )
    }

    items(dashboardOrder, key = { it }) { module ->
        val itemModifier = Modifier.padding(horizontal = 16.dp)
        
        when (module) {
            Constants.Modules.TASKS -> DashboardTaskCard(
                pendingCount = pendingTasksCount,
                onClick = { navController.navigate(Screen.Tasks) },
                modifier = itemModifier
            )
            Constants.Modules.PETS -> DashboardPetCard(
                petSummary = petData,
                onClick = { navController.navigate(Screen.Family) },
                modifier = itemModifier
            )
            Constants.Modules.CALENDAR -> DashboardCalendarCard(
                eventSummary = nextEvent,
                onClick = { navController.navigate(Screen.Calendar) },
                modifier = itemModifier
            )
            Constants.Modules.EXPENSES -> DashboardFinanceCard(
                total = monthlyExpense,
                onClick = { navController.navigate(Screen.Expenses) },
                modifier = itemModifier
            )
            Constants.Modules.REWARDS -> DashboardRewardCard(
                points = userPoints,
                level = userLevel,
                onClick = { navController.navigate(Screen.RewardStore) },
                modifier = itemModifier
            )
            Constants.Modules.POSTITS -> {
                Column(modifier = itemModifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.module_postits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onAddPostIt) {
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = stringResource(R.string.dashboard_new_postit),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    if (postIts.isEmpty()) {
                        Text(
                            text = stringResource(R.string.dashboard_postit_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    } else {
                        PostItSection(postIts = postIts, onEditPostIt = onEditPostIt, onDeletePostIt = onDeletePostIt)
                    }
                }
            }
        }
    }

    item {
        QuickActionsRow(
            navController = navController,
            actions = quickActions,
            onAddPostIt = onAddPostIt
        )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    user: User?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onCustomizeClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .heightIn(min = 48.dp),
                placeholder = { Text(stringResource(R.string.dashboard_search_placeholder), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        },
        actions = {
            IconButton(onClick = onCustomizeClick) {
                Icon(Icons.Default.DashboardCustomize, contentDescription = stringResource(R.string.cd_customize), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSettingsClick) {
                val avatarUrl = user?.avatarUrl
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, contentDescription = stringResource(R.string.cd_settings), tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun FamilyStatusRow(
  members: List<FamilyMember>,
  onMoodClick: (FamilyMember) -> Unit
) {
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Text(
      text = stringResource(R.string.dashboard_family_status),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 12.dp)
    )
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
      items(members, key = { it.id }) { member ->
        MoodAvatar(member = member, onMoodClick = { onMoodClick(member) })
      }
    }
  }
}

@Composable
fun MoodAvatar(member: FamilyMember, onMoodClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable { onMoodClick() }
  ) {
    Box(modifier = Modifier.size(64.dp)) {
      Surface(
        modifier = Modifier.fillMaxSize(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        border = if (member.estadoAnimo != null) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
      ) {
        val imageModel = member.fotoUri ?: member.urlNube
        if (imageModel != null) {
          AsyncImage(
            model = imageModel,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop
          )
        } else {
          Icon(
            imageVector = if (member.tipo == TipoMiembro.PERSONA) Icons.Default.Person else Icons.Default.Pets,
            contentDescription = null,
            modifier = Modifier.size(32.dp).align(Alignment.Center),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
      
      member.estadoAnimo?.let { emoji ->
        if (emoji.isNotBlank()) {
          Surface(
            modifier = Modifier.size(24.dp).align(Alignment.BottomEnd),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = emoji, fontSize = 12.sp)
            }
          }
        }
      }
    }
    Text(
      text = member.nombre,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.padding(top = 6.dp)
    )
  }
}

@Composable
fun QuickActionsRow(navController: NavController, actions: List<String>, onAddPostIt: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        Text(
            text = stringResource(R.string.dashboard_quick_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions.forEach { actionCode ->
                val (icon, label, screen) = when (actionCode) {
                    "CALC_DOSIS" -> Triple(Icons.AutoMirrored.Filled.NoteAdd, stringResource(R.string.action_dosage), Screen.DosageCalculator)
                    "UTIL_PDF" -> Triple(Icons.Default.PictureAsPdf, stringResource(R.string.action_pdf), Screen.PhotoToPdf)
                    "UTIL_SAFE" -> Triple(Icons.Default.Lock, stringResource(R.string.action_safe), Screen.SmartSafe)
                    "CALC_IMC" -> Triple(Icons.Default.MonitorWeight, stringResource(R.string.action_bmi), Screen.BMICalculator)
                    "AGE_CALC" -> Triple(Icons.Default.CalendarToday, stringResource(R.string.util_age_title), Screen.AgeCalculator)
                    "SAVINGS" -> Triple(Icons.Default.Payments, stringResource(R.string.util_savings_title), Screen.SavingsCalculator)
                    "CONSUMPTION" -> Triple(Icons.Default.Build, stringResource(R.string.util_consumption_title), Screen.ConsumptionCalculator)
                    "VEHICLE" -> Triple(Icons.Default.Home, stringResource(R.string.util_vehicle_title), Screen.VehicleManager)
                    else -> Triple(Icons.Default.Apps, stringResource(R.string.action_extra), Screen.Utilities)
                }
                
                QuickActionButton(
                    icon = icon, 
                    label = label, 
                    onClick = { 
                        if (screen != null) navController.navigate(screen)
                    }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(16.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun DashboardTaskCard(pendingCount: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCasaCard(onClick = onClick, useGlassmorphism = true, modifier = modifier) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.module_tasks), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = if (pendingCount == "0") stringResource(R.string.tasks_all_done) else "$pendingCount ${stringResource(R.string.tasks_to_do)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun DashboardPetCard(petSummary: PetSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val summary = if (petSummary.totalCount == 0) {
        stringResource(R.string.dashboard_no_pets)
    } else {
        val detail = petSummary.typeCounts.mapNotNull { (tipo, count) ->
            when (tipo) {
                TipoMiembro.PERRO -> pluralStringResource(R.plurals.dashboard_pets_summary_dogs, count, count)
                TipoMiembro.GATO -> pluralStringResource(R.plurals.dashboard_pets_summary_cats, count, count)
                TipoMiembro.TORTUGA -> pluralStringResource(R.plurals.dashboard_pets_summary_turtles, count, count)
                else -> null
            }
        }.joinToString(" · ")
        detail.ifEmpty { stringResource(R.string.dashboard_pet_fallback) }
    }

    AppCasaCard(onClick = onClick, useGlassmorphism = true, modifier = modifier) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)) {
                Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.module_pets), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun DashboardCalendarCard(eventSummary: NextEventSummary?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val text = if (eventSummary != null) {
        val dateStr = formatEventDate(eventSummary.timestamp)
        val title = if (eventSummary.isBirthday) {
            stringResource(R.string.dashboard_birthday_event_format, eventSummary.title)
        } else {
            eventSummary.title
        }
        "$title · $dateStr"
    } else {
        stringResource(R.string.dashboard_no_events_upcoming)
    }

    AppCasaCard(onClick = onClick, useGlassmorphism = true, modifier = modifier) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.tertiary)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.module_calendar), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun formatEventDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val cal = Calendar.getInstance().apply { time = date }
    val isAllDay = cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0
    
    val pattern = if (isAllDay) {
        stringResource(R.string.dashboard_event_date_all_day_format, stringResource(R.string.dashboard_event_all_day))
    } else {
        stringResource(R.string.dashboard_event_date_time_format)
    }
    
    return remember(timestamp, pattern) {
        SimpleDateFormat(pattern, Constants.Locales.SPAIN).format(date)
    }
}

@Composable
fun DashboardFinanceCard(total: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCasaCard(onClick = onClick, useGlassmorphism = true, modifier = modifier) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color(0xFF4CAF50).copy(alpha = 0.1f)) {
                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.padding(10.dp), tint = Color(0xFF4CAF50))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.module_expenses), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "${stringResource(R.string.expenses_budget)}: $total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun DashboardRewardCard(points: Int, level: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCasaCard(onClick = onClick, useGlassmorphism = true, modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.dashboard_rewards_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.dashboard_rewards_status, level, points), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(16.dp))
            val progress = (points % 100) / 100f
            PremiumProgressBar(
                progress = progress,
                label = stringResource(R.string.dashboard_rewards_next_level),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItSection(postIts: List<PostIt>, onEditPostIt: (PostIt) -> Unit, onDeletePostIt: (PostIt) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
    ) {
        itemsIndexed(postIts, key = { _, it -> it.id }) { index, postIt ->
            // Rotación alterna para efecto "nevera"
            val rotation = if (index % 2 == 0) -3f else 2f
            PostItCard(
                postIt = postIt, 
                rotation = rotation,
                onDoubleClick = { onEditPostIt(postIt) },
                onDelete = { onDeletePostIt(postIt) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItCard(postIt: PostIt, rotation: Float, onDoubleClick: () -> Unit, onDelete: () -> Unit) {
  Surface(
    modifier = Modifier
        .size(width = 150.dp, height = 150.dp)
        .graphicsLayer { rotationZ = rotation }
        .combinedClickable(
            onClick = {},
            onDoubleClick = onDoubleClick
        ),
    color = Color(android.graphics.Color.parseColor(postIt.colorHex)),
    shape = RoundedCornerShape(2.dp),
    shadowElevation = 6.dp
  ) {
    Box(modifier = Modifier.padding(12.dp)) {
      // Efecto sutil de "chincheta" o cinta
      Box(
          modifier = Modifier
              .size(24.dp, 8.dp)
              .background(Color.White.copy(alpha = 0.3f))
              .align(Alignment.TopCenter)
      )
      
      Text(
        text = postIt.contenido,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Black.copy(alpha = 0.7f),
        lineHeight = 20.sp,
        modifier = Modifier.padding(top = 8.dp)
      )
      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(24.dp).align(Alignment.BottomEnd)
      ) {
        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Black.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
      }

      Box(modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)) {
        SyncStatusBadge(isSynced = postIt.lastSyncedAt != null && postIt.lastSyncedAt!! >= postIt.updatedAt)
      }
    }
  }
}

@Composable
fun AddPostItDialog(initialContent: String = "", onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var content by remember { mutableStateOf(initialContent) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialContent.isEmpty()) stringResource(R.string.dashboard_new_postit) else stringResource(R.string.dashboard_edit_note)) },
        text = {
            OutlinedTextField(
                value = content, 
                onValueChange = { content = it }, 
                placeholder = { Text(stringResource(R.string.dashboard_postit_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (content.isNotBlank()) onConfirm(content) }) { 
                Text(if (initialContent.isEmpty()) stringResource(R.string.dashboard_postit_confirm) else stringResource(R.string.dashboard_save)) 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodSelectorDialog(member: FamilyMember, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val emojis = listOf("😊", "🥰", "😴", "🤢", "😤", "😢", "😎", "🤩", "🤔", "🥳")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dashboard_mood_question, member.nombre)) },
        text = {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 5
                ) {
                    emojis.forEach { emoji ->
                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(48.dp)
                                .clickable { onSelect(emoji) },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = { onSelect("") }, // Clear mood
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dashboard_mood_clear))
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun DashboardCustomizerDialog(
    currentOrder: List<String>, // Esta ahora trae la lista completa (activos y HIDDEN_)
    currentQuickActions: List<String>,
    onDismiss: () -> Unit, 
    onSaveOrder: (List<String>) -> Unit,
    onSaveQuickActions: (List<String>) -> Unit
) {
    // Estado interno para manejar el orden y visibilidad
    var fullListOrder by remember { mutableStateOf(currentOrder) }
    var actions by remember { mutableStateOf(currentQuickActions) }
    var selectedTab by remember { mutableStateOf(0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dashboard_customize)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text(stringResource(R.string.dashboard_tab_modules), modifier = Modifier.padding(8.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text(stringResource(R.string.dashboard_tab_actions), modifier = Modifier.padding(8.dp))
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                if (selectedTab == 0) {
                    Text(stringResource(R.string.dashboard_customizer_desc_modules), style = MaterialTheme.typography.bodySmall)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                        itemsIndexed(fullListOrder) { index, item ->
                            val isHidden = item.startsWith("HIDDEN_")
                            val moduleKey = if (isHidden) item.substring(7) else item
                            
                            val label = when(moduleKey) {
                                Constants.Modules.TASKS -> stringResource(R.string.module_tasks)
                                Constants.Modules.PETS -> stringResource(R.string.module_pets)
                                Constants.Modules.CALENDAR -> stringResource(R.string.module_calendar)
                                Constants.Modules.EXPENSES -> stringResource(R.string.module_expenses)
                                Constants.Modules.POSTITS -> stringResource(R.string.module_postits)
                                Constants.Modules.REWARDS -> stringResource(R.string.module_rewards)
                                else -> stringResource(R.string.cd_customize)
                            }
                            
                            CustomizerModuleItem(
                                label = label,
                                isVisible = !isHidden,
                                onToggle = {
                                    val newList = fullListOrder.toMutableList()
                                    newList[index] = if (isHidden) moduleKey else "HIDDEN_$moduleKey"
                                    fullListOrder = newList
                                },
                                onUp = { 
                                    if (index > 0) {
                                        val newList = fullListOrder.toMutableList()
                                        val element = newList.removeAt(index)
                                        newList.add(index - 1, element)
                                        fullListOrder = newList
                                    }
                                },
                                onDown = {
                                    if (index < fullListOrder.size - 1) {
                                        val newList = fullListOrder.toMutableList()
                                        val element = newList.removeAt(index)
                                        newList.add(index + 1, element)
                                        fullListOrder = newList
                                    }
                                },
                                isFirst = index == 0,
                                isLast = index == fullListOrder.size - 1
                            )
                        }
                    }
                } else {
                    Text(stringResource(R.string.dashboard_customizer_desc_actions), style = MaterialTheme.typography.bodySmall)
                    val allActions = listOf(
                        "CALC_DOSIS" to stringResource(R.string.action_dosage), 
                        "UTIL_PDF" to stringResource(R.string.action_pdf), 
                        "UTIL_SAFE" to stringResource(R.string.action_safe), 
                        "CALC_IMC" to stringResource(R.string.action_bmi),
                        "AGE_CALC" to stringResource(R.string.util_age_title),
                        "SAVINGS" to stringResource(R.string.util_savings_title),
                        "CONSUMPTION" to stringResource(R.string.util_consumption_title),
                        "VEHICLE" to stringResource(R.string.util_vehicle_title)
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                        items(allActions) { (code, label) ->
                            val isSelected = actions.contains(code)
                            ListItem(
                                headlineContent = { Text(label) },
                                trailingContent = {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked && actions.size < 4) {
                                                actions = actions + code
                                            } else if (!checked) {
                                                actions = actions - code
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier.clickable {
                                    if (!isSelected && actions.size < 4) actions = actions + code
                                    else if (isSelected) actions = actions - code
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSaveOrder(fullListOrder)
                onSaveQuickActions(actions)
                onDismiss()
            }) { Text(stringResource(R.string.dashboard_done)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) }
        }
    )
}

@Composable
fun CustomizerModuleItem(
    label: String, 
    isVisible: Boolean,
    onToggle: () -> Unit,
    onUp: () -> Unit, 
    onDown: () -> Unit, 
    isFirst: Boolean, 
    isLast: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isVisible) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Switch(
                    checked = isVisible,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(0.7f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = label, 
                    fontWeight = FontWeight.Bold,
                    color = if (isVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            Row {
                IconButton(onClick = onUp, enabled = !isFirst) {
                    Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDown, enabled = !isLast) {
                    Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SearchResultsOverlay(results: List<SearchItem>, onResultClick: (SearchItem) -> Unit, onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.dashboard_search_results_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
            
            if (results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.dashboard_search_no_results), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { item ->
                        ListItem(
                            headlineContent = { Text(item.title, fontWeight = FontWeight.Bold) },
                            supportingContent = { Text(item.type.name) },
                            leadingContent = { 
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                                    Icon(item.icon, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onResultClick(item) }
                        )
                    }
                }
            }
        }
    }
}

