package com.appcasa.features.settings.presentation.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.features.presentation.viewmodel.DashboardViewModel
import com.appcasa.features.settings.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  navController: NavController,
  innerPadding: PaddingValues,
  dashboardViewModel: DashboardViewModel = hiltViewModel(),
  settingsViewModel: SettingsViewModel = hiltViewModel()
) {
  val usuario by settingsViewModel.usuarioActual.collectAsState()
  val hogar by settingsViewModel.hogarActual.collectAsState()
  val configs by settingsViewModel.configuraciones.collectAsState()
  val listas by settingsViewModel.todasLasListas.collectAsState()
  
  var seedStatus by remember { mutableStateOf<String?>(null) }

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(
      title = { Text("Ajustes") },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary
      )
    )
    
    if (seedStatus != null) {
      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(seedStatus!!, style = MaterialTheme.typography.bodySmall)
          Spacer(Modifier.width(8.dp))
          TextButton(onClick = { seedStatus = null }) { Text("OK") }
        }
      }
    }

    SettingsContent(
      modifier = Modifier.weight(1f),
      userName = usuario?.nombre ?: "Usuario",
      householdName = hogar?.nombre ?: "Mi Hogar",
      configs = configs,
      listas = listas,
      onUpdateName = { settingsViewModel.updateUsuario(it) },
      onUpdateHouseholdName = { settingsViewModel.updateHogar(it) },
      onUpdateConfig = { clave, valor -> settingsViewModel.updateConfig(clave, valor) },
      onSeedData = { 
        dashboardViewModel.seedRealData(hogar?.id ?: 1L)
        seedStatus = "¡Datos de Erick y familia cargados con éxito!"
      }
    )
  }
}

@Composable
fun SettingsContent(
  modifier: Modifier = Modifier,
  userName: String,
  householdName: String,
  configs: Map<String, String>,
  listas: List<com.appcasa.features.lists.data.local.ListaEntity>,
  onUpdateName: (String) -> Unit,
  onUpdateHouseholdName: (String) -> Unit,
  onUpdateConfig: (String, String) -> Unit,
  onSeedData: () -> Unit
) {
  val darkMode = configs["tema_oscuro"] == "true"
  val notificationsEnabled = configs["notif_activas"] != "false"
  val currency = configs["moneda"] ?: "€"
  val shopMode = configs["modo_tienda"] == "true"
  val compactView = configs["vista_compacta"] == "true"
  val preferredListId = configs["lista_compra_id"]?.toLongOrNull()
  val preferredListName = listas.find { it.id == preferredListId }?.nombre ?: "No seleccionada"

  var showNameDialog by remember { mutableStateOf(false) }
  var showHogarDialog by remember { mutableStateOf(false) }
  var showCurrencyDialog by remember { mutableStateOf(false) }
  var showListSelector by remember { mutableStateOf(false) }

  if (showNameDialog) {
    EditValueDialog(
      title = "Nombre de Usuario",
      initialValue = userName,
      onDismiss = { showNameDialog = false },
      onConfirm = { onUpdateName(it); showNameDialog = false }
    )
  }

  if (showHogarDialog) {
    EditValueDialog(
      title = "Nombre del Hogar",
      initialValue = householdName,
      onDismiss = { showHogarDialog = false },
      onConfirm = { onUpdateHouseholdName(it); showHogarDialog = false }
    )
  }

  if (showCurrencyDialog) {
    CurrencySelectorDialog(
      selectedCurrency = currency,
      onDismiss = { showCurrencyDialog = false },
      onSelect = { onUpdateConfig("moneda", it); showCurrencyDialog = false }
    )
  }

  if (showListSelector) {
    ListSelectorDialog(
      listas = listas,
      selectedListId = preferredListId,
      onDismiss = { showListSelector = false },
      onSelect = { onUpdateConfig("lista_compra_id", it.toString()); showListSelector = false }
    )
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    item { SettingsSectionHeader("Perfil del Hogar") }
    item {
      SettingsItem(
        icon = Icons.Default.Person,
        title = "Nombre del Usuario",
        subtitle = userName,
        onClick = { showNameDialog = true }
      )
    }
    item {
      SettingsItem(
        icon = Icons.Default.Home,
        title = "Nombre del Hogar",
        subtitle = householdName,
        onClick = { showHogarDialog = true }
      )
    }

    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
    item { SettingsSectionHeader("Apariencia y Notificaciones") }
    
    item {
      SettingsToggleItem(
        icon = Icons.Default.DarkMode,
        title = "Modo Oscuro",
        subtitle = "Cambiar el tema de la aplicación",
        checked = darkMode,
        onCheckedChange = { onUpdateConfig("tema_oscuro", it.toString()) }
      )
    }
    item {
      SettingsToggleItem(
        icon = Icons.Default.Notifications,
        title = "Notificaciones",
        subtitle = "Recibir avisos de tareas y eventos",
        checked = notificationsEnabled,
        onCheckedChange = { onUpdateConfig("notif_activas", it.toString()) }
      )
    }
    item {
      SettingsToggleItem(
        icon = Icons.Default.Compress,
        title = "Vista Compacta",
        subtitle = "Mostrar listas con menos espacio",
        checked = compactView,
        onCheckedChange = { onUpdateConfig("vista_compacta", it.toString()) }
      )
    }

    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
    item { SettingsSectionHeader("Preferencias de Uso") }

    item {
      SettingsItem(
        icon = Icons.Default.Payments,
        title = "Moneda Local",
        subtitle = "Símbolo: $currency",
        onClick = { showCurrencyDialog = true }
      )
    }
    item {
      SettingsItem(
        icon = Icons.Default.ShoppingCart,
        title = "Lista de la Compra Principal",
        subtitle = preferredListName,
        onClick = { showListSelector = true }
      )
    }
    item {
      SettingsToggleItem(
        icon = Icons.Default.Storefront,
        title = "Modo Tienda",
        subtitle = "Mantener pantalla encendida en listas",
        checked = shopMode,
        onCheckedChange = { onUpdateConfig("modo_tienda", it.toString()) }
      )
    }

    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
    item { SettingsSectionHeader("Sistema") }

    item {
      SettingsItem(
        icon = Icons.Default.CloudUpload,
        title = "Actualizar Datos Oficiales",
        subtitle = "Cargar Erick, Alicia, Brian y mascotas",
        onClick = onSeedData
      )
    }
    
    item {
      SettingsItem(
        icon = Icons.Default.Info,
        title = "Acerca de AppCasa",
        subtitle = "Versión 1.0.0-Beta",
        onClick = {}
      )
    }
    
    item {
      Spacer(modifier = Modifier.height(24.dp))
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
          text = "Hecho con ❤️ para la familia",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
      }
    }
  }
}

@Composable
fun EditValueDialog(
  title: String,
  initialValue: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var text by remember { mutableStateOf(initialValue) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
    },
    confirmButton = {
      Button(onClick = { if (text.isNotBlank()) onConfirm(text) }) { Text("Guardar") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    }
  )
}

@Composable
fun CurrencySelectorDialog(
  selectedCurrency: String,
  onDismiss: () -> Unit,
  onSelect: (String) -> Unit
) {
  val currencies = listOf("€", "$", "£", "¥", "₣")
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Seleccionar Moneda") },
    text = {
      Column {
        currencies.forEach { curr ->
          Row(
            modifier = Modifier.fillMaxWidth().clickable { onSelect(curr) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = curr == selectedCurrency, onClick = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(curr)
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
  )
}

@Composable
fun ListSelectorDialog(
  listas: List<com.appcasa.features.lists.data.local.ListaEntity>,
  selectedListId: Long?,
  onDismiss: () -> Unit,
  onSelect: (Long) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Lista de la Compra") },
    text = {
      if (listas.isEmpty()) {
        Text("No hay listas creadas. Crea una en la sección de Listas.")
      } else {
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
          items(listas) { lista ->
            Row(
              modifier = Modifier.fillMaxWidth().clickable { onSelect(lista.id) }.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(selected = lista.id == selectedListId, onClick = null)
              Spacer(modifier = Modifier.width(12.dp))
              Text(lista.nombre)
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
  )
}

@Composable
fun SettingsSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.primary,
    fontWeight = FontWeight.Bold,
    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
  )
}

@Composable
fun SettingsItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  com.appcasa.core.ui.components.AppCasaCard(
    onClick = onClick,
    useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth()
  ) {
    ListItem(
      headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
      supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
      leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
      colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
  }
}

@Composable
fun SettingsToggleItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  com.appcasa.core.ui.components.AppCasaCard(
    useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth()
  ) {
    ListItem(
      headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
      supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
      leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
      trailingContent = {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
      },
      colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
  }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
  AppCasaTheme {
    SettingsContent(
      userName = "Juan",
      householdName = "Villa Casa",
      configs = emptyMap(),
      listas = emptyList(),
      onUpdateName = {},
      onUpdateHouseholdName = {},
      onUpdateConfig = { _, _ -> },
      onSeedData = {}
    )
  }
}
