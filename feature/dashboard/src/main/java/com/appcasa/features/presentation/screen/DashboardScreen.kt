package com.appcasa.features.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.core.utils.Constants
import com.appcasa.feature.dashboard.R
import com.appcasa.features.dashboard.data.local.PostItEntity
import com.appcasa.features.dashboard.presentation.model.SearchItem
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.presentation.viewmodel.DashboardViewModel
import com.appcasa.navigation.Screen
import kotlin.random.Random

@Composable
fun DashboardScreen(
  navController: NavController,
  viewModel: DashboardViewModel = hiltViewModel()
) {
  val petData by viewModel.petData.collectAsState()
  val pendingTasks by viewModel.pendingTasksCount.collectAsState()
  val nextEventData by viewModel.nextEventData.collectAsState()
  val monthlyExpense by viewModel.monthlyExpense.collectAsState()
  val lowStockCount by viewModel.lowStockCount.collectAsState()
  
  val searchQuery by viewModel.searchQuery.collectAsState()
  val searchResults by viewModel.searchResults.collectAsState()
  
  val postIts by viewModel.postIts.collectAsState()
  val dashboardOrder by viewModel.dashboardOrder.collectAsState()
  val familyMembers by viewModel.familyMembers.collectAsState()

  AppCasaMeshBackground {
    PullToRefreshWrapper {
      DashboardContent(
        petCount = petData.first,
        petSummary = petData.second,
        pendingTasks = pendingTasks,
        nextEvent = nextEventData.first,
        nextEventDate = nextEventData.second,
        monthlyExpense = monthlyExpense,
        lowStockCount = lowStockCount,
        searchQuery = searchQuery,
        searchResults = searchResults,
        postIts = postIts,
        dashboardOrder = dashboardOrder,
        familyMembers = familyMembers,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onAddPostIt = { viewModel.addPostIt(it) },
        onEditPostIt = { postIt, newContent -> 
            viewModel.deletePostIt(postIt)
            viewModel.addPostIt(newContent, postIt.colorHex)
        },
        onDeletePostIt = { viewModel.deletePostIt(it) },
        onUpdateMood = { id, emoji -> viewModel.updateMemberMood(id, emoji) },
        onReorder = { viewModel.updateDashboardOrder(it) },
        onResultClick = { item ->
          navController.navigate(item.route)
        },
        onNavigateToTasks = { 
          navController.navigate(Screen.Tasks.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = false }
            launchSingleTop = true
            restoreState = false
          }
        },
        onNavigateToFamily = { 
          navController.navigate(Screen.Family.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = false }
            launchSingleTop = true
            restoreState = false
          }
        },
        onNavigateToCalendar = { 
          navController.navigate(Screen.Calendar.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = false }
            launchSingleTop = true
            restoreState = false
          }
        },
        onNavigateToUtilities = { 
          navController.navigate(Screen.Utilities.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = false }
            launchSingleTop = true
            restoreState = false
          }
        },
        onNavigateToLists = { 
          navController.navigate(Screen.Lists.route)
        },
        onNavigateToSettings = {
          navController.navigate(Screen.Settings.route)
        },
        onNavigateToExpenses = {
          navController.navigate(Screen.Expenses.route)
        },
        onNavigateToInventory = {
          navController.navigate(Screen.Inventory.route)
        },
        onNavigateToDosage = {
          navController.navigate(Screen.DosageCalculator.route)
        },
        onNavigateToPdf = {
          navController.navigate(Screen.PhotoToPdf.route)
        },
        onNavigateToSafe = {
          navController.navigate(Screen.SmartSafe.route)
        }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
  petCount: String,
  petSummary: String,
  pendingTasks: String,
  nextEvent: String,
  nextEventDate: String,
  monthlyExpense: String,
  lowStockCount: Int,
  searchQuery: String,
  searchResults: List<SearchItem>,
  postIts: List<PostItEntity>,
  dashboardOrder: List<String>,
  familyMembers: List<MiembroEntity>,
  onSearchQueryChange: (String) -> Unit,
  onAddPostIt: (String) -> Unit,
  onEditPostIt: (PostItEntity, String) -> Unit,
  onDeletePostIt: (PostItEntity) -> Unit,
  onUpdateMood: (Long, String) -> Unit,
  onReorder: (List<String>) -> Unit,
  onResultClick: (SearchItem) -> Unit,
  onNavigateToTasks: () -> Unit,
  onNavigateToFamily: () -> Unit,
  onNavigateToCalendar: () -> Unit,
  onNavigateToUtilities: () -> Unit,
  onNavigateToLists: () -> Unit,
  onNavigateToSettings: () -> Unit = {},
  onNavigateToExpenses: () -> Unit = {},
  onNavigateToInventory: () -> Unit = {},
  onNavigateToDosage: () -> Unit = {},
  onNavigateToPdf: () -> Unit = {},
  onNavigateToSafe: () -> Unit = {}
) {
  var searchActive by remember { mutableStateOf(false) }
  var showPostItDialog by remember { mutableStateOf(false) }
  var showReorderDialog by remember { mutableStateOf(false) }
  var postItText by remember { mutableStateOf("") }

  if (showPostItDialog) {
    AlertDialog(
        onDismissRequest = { showPostItDialog = false },
        title = { Text(stringResource(R.string.dashboard_new_postit)) },
        text = {
            OutlinedTextField(
                value = postItText,
                onValueChange = { postItText = it },
                placeholder = { Text(stringResource(R.string.dashboard_postit_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
        },
        confirmButton = {
            Button(onClick = {
                if (postItText.isNotBlank()) {
                    onAddPostIt(postItText)
                    postItText = ""
                    showPostItDialog = false
                }
            }) { Text(stringResource(R.string.dashboard_postit_confirm)) }
        }
    )
  }

  if (showReorderDialog) {
    val modules = listOf(
        Constants.Modules.TASKS to stringResource(R.string.module_tasks),
        Constants.Modules.PETS to stringResource(R.string.module_pets),
        Constants.Modules.CALENDAR to stringResource(R.string.module_calendar),
        Constants.Modules.EXPENSES to stringResource(R.string.module_expenses),
        Constants.Modules.POSTITS to stringResource(R.string.module_postits)
    )
    AlertDialog(
        onDismissRequest = { showReorderDialog = false },
        title = { Text(stringResource(R.string.dashboard_customize)) },
        text = {
            Column {
                Text(stringResource(R.string.dashboard_reorder_instruction), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                dashboardOrder.forEachIndexed { index, code ->
                    val name = modules.find { it.first == code }?.second ?: code
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            if (index > 0) {
                                val newList = dashboardOrder.toMutableList()
                                java.util.Collections.swap(newList, index, index - 1)
                                onReorder(newList)
                            }
                        }) { Icon(Icons.Default.ArrowUpward, contentDescription = null) }
                        IconButton(onClick = {
                            if (index < dashboardOrder.size - 1) {
                                val newList = dashboardOrder.toMutableList()
                                java.util.Collections.swap(newList, index, index + 1)
                                onReorder(newList)
                            }
                        }) { Icon(Icons.Default.ArrowDownward, contentDescription = null) }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { showReorderDialog = false }) { Text(stringResource(R.string.dashboard_done)) } }
    )
  }

  Scaffold(
    topBar = {
      if (!searchActive) {
        TopAppBar(
          title = {
            Column {
              Text(
                text = stringResource(com.appcasa.core.ui.R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
              )
              Text(
                text = stringResource(R.string.dashboard_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
              )
            }
          },
          actions = {
            IconButton(onClick = { showPostItDialog = true }) {
                Icon(Icons.Default.StickyNote2, contentDescription = stringResource(R.string.cd_postit), tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = { showReorderDialog = true }) {
                Icon(Icons.Default.SettingsSuggest, contentDescription = stringResource(R.string.cd_customize), tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = { searchActive = true }) {
              Icon(Icons.Default.Search, contentDescription = stringResource(R.string.cd_search), tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onNavigateToSettings) {
              Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings), tint = MaterialTheme.colorScheme.onPrimary)
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
          )
        )
      }
    },
    containerColor = Color.Transparent
  ) { scaffoldPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
      
      if (searchActive) {
        SearchBar(
          query = searchQuery,
          onQueryChange = onSearchQueryChange,
          onSearch = { },
          active = searchActive,
          onActiveChange = { searchActive = it },
          placeholder = { Text(stringResource(R.string.dashboard_search_placeholder)) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = { 
            IconButton(onClick = { 
              if (searchQuery.isNotEmpty()) onSearchQueryChange("") else searchActive = false 
            }) {
              Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
            }
          },
          modifier = Modifier.fillMaxWidth().padding(horizontal = if (searchActive) 0.dp else 16.dp)
        ) {
          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(searchResults) { item ->
              ListItem(
                headlineContent = { Text(item.title) },
                supportingContent = { Text(item.type.name) },
                leadingContent = { Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable { 
                  searchActive = false
                  onResultClick(item) 
                }
              )
            }
            if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
              item {
                Column(
                  modifier = Modifier.fillMaxWidth().padding(32.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = Icons.Default.SearchOff, 
                    contentDescription = null, 
                    modifier = Modifier.size(64.dp), 
                    tint = MaterialTheme.colorScheme.outline
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Text(stringResource(R.string.dashboard_no_results, searchQuery), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                }
              }
            }
          }
        }
      }

      AnimatedVisibility(
        visible = !searchActive,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Text(
              text = stringResource(R.string.dashboard_family_status),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                familyMembers.filter { it.tipo == TipoMiembro.PERSONA.name }.forEach { member ->
                    MoodAvatar(member = member, onMoodClick = { onUpdateMood(member.id, it) })
                }
            }
          }

          dashboardOrder.forEach { module ->
              when(module) {
                  Constants.Modules.TASKS -> item { 
                    DashboardCard(
                      icon = Icons.Default.CheckCircle,  
                      title = stringResource(R.string.module_tasks), 
                      value = pendingTasks, 
                      subtitle = if (pendingTasks == "0") stringResource(R.string.tasks_all_done) else stringResource(R.string.tasks_to_do),
                      onClick = onNavigateToTasks
                    ) 
                  }
                  Constants.Modules.PETS -> item { 
                    DashboardCard(
                      icon = Icons.Default.Pets,          
                      title = stringResource(R.string.module_pets), 
                      value = petCount,  
                      subtitle = petSummary,
                      onClick = onNavigateToFamily
                    ) 
                  }
                  Constants.Modules.CALENDAR -> item { 
                    DashboardCard(
                      icon = Icons.Default.CalendarMonth, 
                      title = stringResource(R.string.module_calendar), 
                      value = nextEvent, 
                      subtitle = nextEventDate,
                      onClick = onNavigateToCalendar
                    ) 
                  }
                  Constants.Modules.EXPENSES -> item { 
                    DashboardCard(
                      icon = Icons.Default.Payments, 
                      title = stringResource(R.string.module_expenses), 
                      value = monthlyExpense, 
                      subtitle = stringResource(R.string.expenses_budget),
                      onClick = onNavigateToExpenses
                    ) 
                  }
                  Constants.Modules.POSTITS -> if (postIts.isNotEmpty()) {
                      item {
                          Text("Notas de la familia", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
                          Row(
                              modifier = Modifier
                                  .fillMaxWidth()
                                  .horizontalScroll(rememberScrollState())
                                  .padding(vertical = 12.dp, horizontal = 16.dp),
                              horizontalArrangement = Arrangement.spacedBy(16.dp)
                          ) {
                              postIts.forEach { postIt ->
                                  PostItCard(
                                      postIt = postIt, 
                                      onDelete = { onDeletePostIt(postIt) },
                                      onEdit = { onEditPostIt(postIt, it) }
                                  )
                              }
                          }
                      }
                  }
              }
          }

          if (lowStockCount > 0) {
            item {
              DashboardCard(
                icon = Icons.Default.Inventory,
                title = stringResource(R.string.inventory_low_stock),
                value = stringResource(R.string.inventory_items_count, lowStockCount),
                subtitle = stringResource(R.string.inventory_needs_restock),
                onClick = onNavigateToInventory,
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
              )
            }
          }
          
          item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = stringResource(R.string.quick_utilities),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Medication,
                label = stringResource(R.string.action_dosage),
                onClick = onNavigateToDosage
              )
              QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PictureAsPdf,
                label = stringResource(R.string.action_pdf),
                onClick = onNavigateToPdf
              )
              QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Lock,
                label = stringResource(R.string.action_safe),
                onClick = onNavigateToSafe
              )
              QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ShoppingCart,
                label = stringResource(R.string.action_shopping),
                onClick = onNavigateToLists
              )
            }
          }
          
          item {
            Spacer(modifier = Modifier.height(16.dp))
          }
        }
      }
    }
  }
}

@Composable
fun MoodAvatar(member: MiembroEntity, onMoodClick: (String) -> Unit) {
    var showMoodPicker by remember { mutableStateOf(false) }
    val emojis = listOf("😊", "😎", "😴", "🤔", "🤒", "😤", "😇")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(60.dp).clip(CircleShape).clickable { showMoodPicker = true },
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (member.fotoUri != null) {
                    AsyncImage(
                        model = member.fotoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(12.dp))
                }
            }
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(member.estadoAnimo ?: "💬", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(member.nombre, style = MaterialTheme.typography.labelSmall)
    }

    if (showMoodPicker) {
        AlertDialog(
            onDismissRequest = { showMoodPicker = false },
            title = { Text(stringResource(R.string.dashboard_mood_question, member.nombre)) },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    emojis.forEach { emoji ->
                        Text(
                            emoji, 
                            modifier = Modifier.clickable { 
                                onMoodClick(emoji)
                                showMoodPicker = false 
                            }.padding(4.dp),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItCard(postIt: PostItEntity, onDelete: () -> Unit, onEdit: (String) -> Unit) {
    var isLifted by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(postIt.contenido) }

    // Rotación aleatoria un poco más pronunciada para el efecto "pegado a mano"
    val rotation = remember(postIt.id) { Random(postIt.id).nextFloat() * 10f - 5f }
    
    val lift by animateFloatAsState(
        targetValue = if (isLifted) -15f else 0f,
        animationSpec = tween(200),
        label = "lift"
    )

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.dashboard_edit_note)) },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (editedText.isNotBlank()) {
                        onEdit(editedText)
                        showEditDialog = false
                    }
                }) { Text(stringResource(R.string.dashboard_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(stringResource(R.string.dashboard_cancel)) }
            }
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(postIt.colorHex))
        ),
        modifier = Modifier
            .size(150.dp)
            .graphicsLayer {
                rotationZ = rotation
                translationY = lift
            }
            .combinedClickable(
                onClick = { isLifted = !isLifted },
                onDoubleClick = { 
                    editedText = postIt.contenido
                    showEditDialog = true 
                }
            ),
        elevation = CardDefaults.cardElevation(if (isLifted) 12.dp else 4.dp),
        shape = RoundedCornerShape(2.dp)
    ) {
        Box(modifier = Modifier.padding(14.dp).fillMaxSize()) {
            Text(
                text = postIt.contenido, 
                style = MaterialTheme.typography.bodyMedium, 
                color = Color.Black.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            
            IconButton(
                onClick = onDelete, 
                modifier = Modifier.align(Alignment.BottomEnd).size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp), 
                    tint = Color.Black.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun DashboardCard(
  icon: ImageVector,
  title: String,
  value: String,
  subtitle: String,
  onClick: () -> Unit = {},
  containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface
) {
  com.appcasa.core.ui.components.AppCasaCard(
    modifier = Modifier.fillMaxWidth(),
    useGlassmorphism = true,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (containerColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.size(56.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (containerColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(28.dp)
          )
        }
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
      }
    }
  }
}

@Composable
private fun QuickActionCard(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  label: String,
  onClick: () -> Unit
) {
  com.appcasa.core.ui.components.AppCasaCard(
    modifier = modifier,
    useGlassmorphism = true,
    onClick = onClick
  ) {
    Column(
      modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp).fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(48.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(24.dp))
        }
      }
      Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
  AppCasaTheme {
    DashboardContent(
      petCount = "7",
      petSummary = "2 perros · 4 gatos · 1 tortuga",
      pendingTasks = "3",
      nextEvent = "Cumpleaños Hijo",
      nextEventDate = "27 de Junio",
      monthlyExpense = "120.50 €",
      lowStockCount = 2,
      searchQuery = "",
      searchResults = emptyList(),
      postIts = emptyList(),
      dashboardOrder = listOf("TASKS", "PETS", "CALENDAR", "EXPENSES", "POSTITS"),
      familyMembers = emptyList(),
      onSearchQueryChange = {},
      onAddPostIt = {},
      onEditPostIt = { _, _ -> },
      onDeletePostIt = {},
      onUpdateMood = { _, _ -> },
      onReorder = {},
      onResultClick = {},
      onNavigateToTasks = {},
      onNavigateToFamily = {},
      onNavigateToCalendar = {},
      onNavigateToUtilities = {},
      onNavigateToLists = {},
      onNavigateToSettings = {},
      onNavigateToExpenses = {},
      onNavigateToInventory = {},
      onNavigateToDosage = {},
      onNavigateToPdf = {},
      onNavigateToSafe = {}
    )
  }
}
