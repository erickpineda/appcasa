package com.appcasa.features.settings.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.feature.settings.R
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
  var showSeedConfirm by remember { mutableStateOf(false) }

  val seedSuccessMessage = stringResource(R.string.settings_seed_success)

  AppCasaConfirmDialog(
    show = showSeedConfirm,
    title = stringResource(R.string.settings_seed_confirm_title),
    text = stringResource(R.string.settings_seed_confirm_text),
    confirmText = stringResource(R.string.settings_seed_confirm_btn),
    icon = Icons.Default.Warning,
    onConfirm = {
        dashboardViewModel.seedRealData(hogar?.id ?: 1L)
        seedStatus = seedSuccessMessage
        showSeedConfirm = false
    },
    onDismiss = { showSeedConfirm = false }
  )

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(
      title = { Text(stringResource(R.string.settings_title)) },
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
          TextButton(onClick = { seedStatus = null }) { Text(stringResource(R.string.settings_ok)) }
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
      onSeedData = { showSeedConfirm = true }
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
  val preferredListName = listas.find { it.id == preferredListId }?.nombre ?: stringResource(R.string.settings_list_not_selected)

  var showNameDialog by remember { mutableStateOf(false) }
  var showHogarDialog by remember { mutableStateOf(false) }
  var showCurrencyDialog by remember { mutableStateOf(false) }
  var showListSelector by remember { mutableStateOf(false) }

  if (showNameDialog) {
    EditValueDialog(
      title = stringResource(R.string.settings_user_name_title),
      initialValue = userName,
      onDismiss = { showNameDialog = false },
      onConfirm = { onUpdateName(it); showNameDialog = false }
    )
  }

  if (showHogarDialog) {
    EditValueDialog(
      title = stringResource(R.string.settings_household_name_title),
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
    item { SettingsSectionHeader(stringResource(R.string.settings_section_profile)) }
    item {
      SettingsItem(
        icon = Icons.Default.Person,
        title = stringResource(R.string.settings_user_name_title),
        subtitle = userName,
        onClick = { showNameDialog = true }
      )
    }
    item {
      SettingsItem(
        icon = Icons.Default.Home,
        title = stringResource(R.string.settings_household_name_title),
        subtitle = householdName,
        onClick = { showHogarDialog = true }
      )
    }

    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
    item { SettingsSectionHeader(stringResource(R.string.settings_section_appearance)) }
    
    item {
      SettingsToggleItem(
        icon = Icons.Default.DarkMode,
        title = stringResource(R.string.settings_dark_mode_title),
        subtitle = stringResource(R.string.settings_dark_mode_subtitle),
        checked = darkMode,
        onCheckedChange = { onUpdateConfig("tema_oscuro", it.toString()) }
      )
    }
    item {
      SettingsToggleItem(
        icon = Icons.Default.Notifications,
        title = stringResource(R.string.settings_notifications_title),
        subtitle = stringResource(R.string.settings_notifications_subtitle),
        checked = notificationsEnabled,
        onCheckedChange = { onUpdateConfig("notif_activas", it.toString()) }
      )
    }
    item {
      SettingsToggleItem(
        icon = Icons.Default.Compress,
        title = stringResource(R.string.settings_compact_view_title),
        subtitle = stringResource(R.string.settings_compact_view_subtitle),
        checked = compactView,
        onCheckedChange = { onUpdateConfig("vista_compacta", it.toString()) }
      )
    }

    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
    item { SettingsSectionHeader(stringResource(R.string.settings_section_preferences)) }

    item {
      SettingsItem(
        icon = Icons.Default.Payments,
        title = stringResource(R.string.settings_currency_title),
        subtitle = stringResource(R.string.settings_currency_subtitle, currency),
        onClick = { showCurrencyDialog = true }
      )
    }
    item {
      SettingsItem(
        icon = Icons.Default.ShoppingCart,
        title = stringResource(R.string.settings_main_list_title),
        subtitle = preferredListName,
        onClick = { showListSelector = true }
      )
    }
    item {
      SettingsToggleItem(
        icon = Icons.Default.Storefront,
        title = stringResource(R.string.settings_shop_mode_title),
        subtitle = stringResource(R.string.settings_shop_mode_subtitle),
        checked = shopMode,
        onCheckedChange = { onUpdateConfig("modo_tienda", it.toString()) }
      )
    }

    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
    item { SettingsSectionHeader(stringResource(R.string.settings_section_system)) }

    item {
      SettingsItem(
        icon = Icons.Default.CloudUpload,
        title = stringResource(R.string.settings_seed_data_title),
        subtitle = stringResource(R.string.settings_seed_data_subtitle),
        onClick = onSeedData
      )
    }
    
    item {
      SettingsItem(
        icon = Icons.Default.Info,
        title = stringResource(R.string.settings_about_title),
        subtitle = stringResource(R.string.settings_about_subtitle),
        onClick = {}
      )
    }
    
    item {
      Spacer(modifier = Modifier.height(24.dp))
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
          text = stringResource(R.string.settings_footer),
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
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
      )
    },
    confirmButton = {
      Button(onClick = { if (text.isNotBlank()) onConfirm(text) }) { Text(stringResource(R.string.settings_btn_save)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_btn_cancel)) }
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
    title = { Text(stringResource(R.string.settings_dialog_currency_title)) },
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
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_btn_close)) } }
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
    title = { Text(stringResource(R.string.settings_dialog_list_title)) },
    text = {
      if (listas.isEmpty()) {
        Text(stringResource(R.string.settings_dialog_no_lists))
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
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_btn_close)) } }
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
