package com.appcasa.features.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.core.ui.components.PullToRefreshWrapper
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.appcasa.core.ui.components.bounceClick
import com.appcasa.features.presentation.viewmodel.DashboardViewModel
import com.appcasa.features.dashboard.presentation.model.SearchItem
import com.appcasa.features.dashboard.data.local.PostItEntity
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.navigation.Screen
import coil3.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@Composable
fun DashboardScreen(
  navController: NavController,
  viewModel: DashboardViewModel = hiltViewModel()
) {
  val petCount by viewModel.petCount.collectAsState()
  val petSummary by viewModel.petSummary.collectAsState()
  val pendingTasks by viewModel.pendingTasksCount.collectAsState()
  val nextEvent by viewModel.nextEvent.collectAsState()
  val nextEventDate by viewModel.nextEventDate.collectAsState()
  val monthlyExpense by viewModel.monthlyExpense.collectAsState()
  val lowStockCount by viewModel.lowStockCount.collectAsState()
  
  val searchQuery by viewModel.searchQuery.collectAsState()
  val searchResults by viewModel.searchResults.collectAsState()
  
  val postIts by viewModel.postIts.collectAsState()
  val dashboardOrder by viewModel.dashboardOrder.collectAsState()
  val familyMembers by viewModel.familyMembers.collectAsState()

  PullToRefreshWrapper {
    DashboardContent(
      petCount = petCount,
      petSummary = petSummary,
      pendingTasks = pendingTasks,
      nextEvent = nextEvent,
      nextEventDate = nextEventDate,
      monthlyExpense = monthlyExpense,
      lowStockCount = lowStockCount,
      searchQuery = searchQuery,
      searchResults = searchResults,
      postIts = postIts,
      dashboardOrder = dashboardOrder,
      familyMembers = familyMembers,
      onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
      onAddPostIt = { viewModel.addPostIt(it) },
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
        navController.navigate(Screen.Management.route) { 
          popUpTo(navController.graph.findStartDestination().id) { saveState = false }
          launchSingleTop = true
          restoreState = false
        }
      },
      onNavigateToSettings = {
        navController.navigate(Screen.Settings.route)
      },
      onNavigateToExpenses = {
        navController.navigate(Screen.Expenses.route)
      },
      onNavigateToInventory = {
        navController.navigate(Screen.Inventory.route)
      }
    )
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
  onNavigateToInventory: () -> Unit = {}
) {
  var searchActive by remember { mutableStateOf(false) }
  var showPostItDialog by remember { mutableStateOf(false) }
  var showReorderDialog by remember { mutableStateOf(false) }
  var postItText by remember { mutableStateOf("") }

  if (showPostItDialog) {
    AlertDialog(
        onDismissRequest = { showPostItDialog = false },
        title = { Text("Nuevo Post-it") },
        text = {
            OutlinedTextField(
                value = postItText,
                onValueChange = { postItText = it },
                placeholder = { Text("Escribe algo para la familia...") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                if (postItText.isNotBlank()) {
                    onAddPostIt(postItText)
                    postItText = ""
                    showPostItDialog = false
                }
            }) { Text("Pegar") }
        }
    )
  }

  if (showReorderDialog) {
    val modules = listOf(
        "TASKS" to "Tareas",
        "PETS" to "Mascotas",
        "CALENDAR" to "Agenda",
        "EXPENSES" to "Gastos",
        "POSTITS" to "Post-its"
    )
    AlertDialog(
        onDismissRequest = { showReorderDialog = false },
        title = { Text("Personalizar Dashboard") },
        text = {
            Column {
                Text("Selecciona el orden de aparición:", style = MaterialTheme.typography.bodySmall)
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
        confirmButton = { Button(onClick = { showReorderDialog = false }) { Text("Hecho") } }
    )
  }

  Scaffold(
    topBar = {
      if (!searchActive) {
        TopAppBar(
          title = {
            Column {
              Text(
                text = "AppCasa",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
              )
              Text(
                text = "Centro de mando familiar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
              )
            }
          },
          actions = {
            IconButton(onClick = { showPostItDialog = true }) {
                Icon(Icons.Default.StickyNote2, contentDescription = "Post-it", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = { showReorderDialog = true }) {
                Icon(Icons.Default.SettingsSuggest, contentDescription = "Personalizar", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = { searchActive = true }) {
              Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onNavigateToSettings) {
              Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.onPrimary)
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
          )
        )
      }
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { scaffoldPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
      
      if (searchActive) {
        SearchBar(
          query = searchQuery,
          onQueryChange = onSearchQueryChange,
          onSearch = { },
          active = searchActive,
          onActiveChange = { searchActive = it },
          placeholder = { Text("Busca tareas, listas, familia...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = { 
            IconButton(onClick = { 
              if (searchQuery.isNotEmpty()) onSearchQueryChange("") else searchActive = false 
            }) {
              Icon(Icons.Default.Close, contentDescription = "Cerrar")
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
                  Text("No hay resultados para \"$searchQuery\"", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
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
              text = "Estado de la familia",
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
                  "TASKS" -> item { 
                    DashboardCard(
                      icon = Icons.Default.CheckCircle,  
                      title = "Tareas pendientes", 
                      value = pendingTasks, 
                      subtitle = if (pendingTasks == "0") "Todo al día" else "Tareas por hacer",
                      onClick = onNavigateToTasks
                    ) 
                  }
                  "PETS" -> item { 
                    DashboardCard(
                      icon = Icons.Default.Pets,          
                      title = "Mascotas", 
                      value = petCount,  
                      subtitle = petSummary,
                      onClick = onNavigateToFamily
                    ) 
                  }
                  "CALENDAR" -> item { 
                    DashboardCard(
                      icon = Icons.Default.CalendarMonth, 
                      title = "Próximo evento", 
                      value = nextEvent, 
                      subtitle = nextEventDate,
                      onClick = onNavigateToCalendar
                    ) 
                  }
                  "EXPENSES" -> item { 
                    DashboardCard(
                      icon = Icons.Default.Payments, 
                      title = "Gastos del mes", 
                      value = monthlyExpense, 
                      subtitle = "Presupuesto familiar",
                      onClick = onNavigateToExpenses
                    ) 
                  }
                  "POSTITS" -> if (postIts.isNotEmpty()) {
                      item {
                          Text("Notas de la familia", style = MaterialTheme.typography.titleSmall)
                          Row(
                              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                              horizontalArrangement = Arrangement.spacedBy(8.dp)
                          ) {
                              postIts.forEach { postIt ->
                                  PostItCard(postIt = postIt, onDelete = { onDeletePostIt(postIt) })
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
                title = "Stock Crítico",
                value = "$lowStockCount artículos",
                subtitle = "Necesitan reposición",
                onClick = onNavigateToInventory,
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
              )
            }
          }
          
          item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Utilidades rápidas",
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
                label = "Dosis",
                onClick = onNavigateToUtilities
              )
              QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ShoppingCart,
                label = "Compra",
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
            title = { Text("¿Cómo estás, ${member.nombre}?") },
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

@Composable
fun PostItCard(postIt: PostItEntity, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(postIt.colorHex))),
        modifier = Modifier.size(140.dp).bounceClick { },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.padding(12.dp).fillMaxSize()) {
            Text(postIt.contenido, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.Black)
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
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
      onNavigateToInventory = {}
    )
  }
}
