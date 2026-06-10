package com.appcasa.features.settings.presentation.screen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.data.utils.FileUtils
import com.appcasa.core.domain.model.Lista
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.core.ui.utils.QRUtils
import com.appcasa.feature.settings.R
import com.appcasa.features.settings.presentation.viewmodel.SettingsUiEvent
import com.appcasa.features.settings.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  navController: NavController,
  innerPadding: PaddingValues,
  settingsViewModel: SettingsViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val usuario by settingsViewModel.usuarioActual.collectAsState()
  val hogar by settingsViewModel.hogarActual.collectAsState()
  val configs by settingsViewModel.configuraciones.collectAsState()
  val listas by settingsViewModel.todasLasListas.collectAsState()
  val isAdmin by settingsViewModel.isAdmin.collectAsState()
  val isSyncing by settingsViewModel.isSyncing.collectAsState()
  val isExporting by settingsViewModel.isExporting.collectAsState()

  LaunchedEffect(Unit) {
    settingsViewModel.settingsEvent.collect { event ->
      when (event) {
        is SettingsUiEvent.ShowToast -> {
          Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
        is SettingsUiEvent.ExportReady -> {
          Toast.makeText(context, "Copia de seguridad generada", Toast.LENGTH_SHORT).show()
          val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, event.content)
            type = "text/plain"
          }
          val shareIntent = Intent.createChooser(sendIntent, "Exportar datos de AppCasa")
          context.startActivity(shareIntent)
        }
      }
    }
  }

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    uri?.let {
      val localPath = FileUtils.saveImageLocally(context, it.toString())
      settingsViewModel.updateUsuario(usuario?.nombre ?: "", localPath)
    }
  }

  SettingsContent(
    userName = usuario?.nombre ?: "Usuario",
    userAvatar = usuario?.avatarUrl,
    householdName = hogar?.nombre ?: "",
    householdCode = hogar?.codigoHogar ?: "---",
    configs = configs,
    listas = listas,
    isAdmin = isAdmin,
    isSyncing = isSyncing,
    isExporting = isExporting,
    onUpdateName = { settingsViewModel.updateUsuario(it) },
    onUpdateAvatar = { imagePickerLauncher.launch("image/*") },
    onUpdateHouseholdName = { settingsViewModel.updateHogar(it) },
    onUpdateConfig = { k, v -> settingsViewModel.updateConfig(k, v) },
    onRegenerateCode = { settingsViewModel.regenerateHouseCode() },
    onForceSync = { settingsViewModel.forceSync() },
    onExportData = { settingsViewModel.exportData() },
    onLogout = { settingsViewModel.logout() }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
  userName: String,
  userAvatar: String?,
  householdName: String,
  householdCode: String,
  configs: Map<String, String>,
  listas: List<Lista>,
  isAdmin: Boolean,
  isSyncing: Boolean,
  isExporting: Boolean,
  onUpdateName: (String) -> Unit,
  onUpdateAvatar: () -> Unit,
  onUpdateHouseholdName: (String) -> Unit,
  onUpdateConfig: (String, String) -> Unit,
  onRegenerateCode: () -> Unit,
  onForceSync: () -> Unit,
  onExportData: () -> Unit,
  onLogout: () -> Unit
) {
  var activeSection by remember { mutableStateOf<SettingsSection?>(null) }

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(
      title = { Text(activeSection?.title ?: stringResource(R.string.settings_title)) },
      navigationIcon = {
        if (activeSection != null) {
          IconButton(onClick = { activeSection = null }) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_back))
          }
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
      )
    )
    
    Box(modifier = Modifier.weight(1f)) {
      if (activeSection == null) {
        SettingsHub(
          userName = userName,
          userAvatar = userAvatar,
          onSectionClick = { activeSection = it },
          onUpdateAvatar = onUpdateAvatar,
          onUpdateName = onUpdateName,
          onLogout = onLogout
        )
      } else {
        when (activeSection!!) {
          SettingsSection.HOUSEHOLD -> HogarSection(
            householdName = householdName,
            householdCode = householdCode,
            isAdmin = isAdmin,
            onUpdateName = onUpdateHouseholdName,
            onRegenerateCode = onRegenerateCode
          )
          SettingsSection.APPEARANCE -> AparienciaSection(
            configs = configs,
            onUpdateConfig = onUpdateConfig
          )
          SettingsSection.PREFERENCES -> PreferenciasSection(
            configs = configs,
            listas = listas,
            onUpdateConfig = onUpdateConfig
          )
          SettingsSection.SYSTEM -> SistemaSection(
            isSyncing = isSyncing,
            isExporting = isExporting,
            onForceSync = onForceSync,
            onExportData = onExportData
          )
        }
      }
    }
  }
}

enum class SettingsSection(val title: String) {
    HOUSEHOLD("Mi Hogar e Invitaciones"),
    APPEARANCE("Pantalla y Notificaciones"),
    PREFERENCES("Preferencias de Uso"),
    SYSTEM("Sistema y Copias de Seguridad")
}

@Composable
fun SettingsHub(
    userName: String,
    userAvatar: String?,
    onSectionClick: (SettingsSection) -> Unit,
    onUpdateAvatar: () -> Unit,
    onUpdateName: (String) -> Unit,
    onLogout: () -> Unit
) {
    var showNameDialog by remember { mutableStateOf(false) }

    if (showNameDialog) {
        EditValueDialog(
            title = stringResource(R.string.settings_edit_name_title),
            initialValue = userName,
            onDismiss = { showNameDialog = false },
            onConfirm = { onUpdateName(it); showNameDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Perfil siempre visible y premium
        item(contentType = "profile") {
            AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { onUpdateAvatar() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (userAvatar != null) {
                            AsyncImage(
                                model = userAvatar,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(modifier = Modifier.weight(1f).clickable { showNameDialog = true }) {
                        Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.settings_profile_edit_hint), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onUpdateAvatar) {
                        Icon(Icons.Default.PhotoCamera, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item(contentType = "header") { SettingsSectionHeader(stringResource(R.string.settings_hub_management)) }
        item(contentType = "category") {
            CategoryItem(
                title = stringResource(R.string.settings_section_household),
                subtitle = stringResource(R.string.settings_hub_household_subtitle),
                icon = Icons.Default.Home,
                onClick = { onSectionClick(SettingsSection.HOUSEHOLD) }
            )
        }

        item(contentType = "header") { SettingsSectionHeader(stringResource(R.string.settings_hub_personalization)) }
        item(contentType = "category") {
            CategoryItem(
                title = stringResource(R.string.settings_section_appearance_full),
                subtitle = stringResource(R.string.settings_hub_appearance_subtitle),
                icon = Icons.Default.NotificationsActive,
                onClick = { onSectionClick(SettingsSection.APPEARANCE) }
            )
        }
        item(contentType = "category") {
            CategoryItem(
                title = stringResource(R.string.settings_hub_preferences),
                subtitle = stringResource(R.string.settings_hub_preferences_subtitle),
                icon = Icons.Default.Payments,
                onClick = { onSectionClick(SettingsSection.PREFERENCES) }
            )
        }

        item(contentType = "header") { SettingsSectionHeader(stringResource(R.string.settings_hub_account)) }
        item(contentType = "category") {
            CategoryItem(
                title = "Sistema y Seguridad",
                subtitle = "Sincronización forzada y backups",
                icon = Icons.Default.Compress,
                onClick = { onSectionClick(SettingsSection.SYSTEM) }
            )
        }
        item(contentType = "category") {
            CategoryItem(
                title = stringResource(R.string.settings_hub_logout),
                subtitle = stringResource(R.string.settings_hub_logout_subtitle),
                icon = Icons.AutoMirrored.Filled.Logout,
                color = MaterialTheme.colorScheme.error,
                onClick = onLogout
            )
        }
        
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.settings_version_footer, "v1.2.0"),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun HogarSection(
    householdName: String,
    householdCode: String,
    isAdmin: Boolean,
    onUpdateName: (String) -> Unit,
    onRegenerateCode: () -> Unit
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var showRegenerateConfirm by remember { mutableStateOf(false) }

    if (showNameDialog) {
        EditValueDialog(
            title = stringResource(R.string.settings_household_name_title),
            initialValue = householdName,
            onDismiss = { showNameDialog = false },
            onConfirm = { onUpdateName(it); showNameDialog = false }
        )
    }

    if (showRegenerateConfirm) {
        AlertDialog(
            onDismissRequest = { showRegenerateConfirm = false },
            title = { Text(stringResource(R.string.settings_regenerate_code_title)) },
            text = { Text(stringResource(R.string.settings_regenerate_code_desc)) },
            confirmButton = {
                Button(onClick = { onRegenerateCode(); showRegenerateConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(R.string.settings_confirm))
                }
            },
            dismissButton = { TextButton(onClick = { showRegenerateConfirm = false }) { Text(stringResource(R.string.settings_btn_cancel)) } }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(contentType = "item") {
            AppCasaCard(onClick = { if (isAdmin) showNameDialog = true }, useGlassmorphism = false) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_household_name_title)) },
                    supportingContent = { Text(householdName) },
                    leadingContent = { Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { if (isAdmin) Icon(Icons.Default.ChevronRight, null) },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
        
        item(contentType = "qr") {
            AppCasaCard(useGlassmorphism = false) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.setup_label_code), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(householdCode, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                        if (isAdmin) {
                            IconButton(onClick = { showRegenerateConfirm = true }) {
                                Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    val qr = remember(householdCode) { QRUtils.generateQRCode(householdCode, 400) }
                    qr?.let {
                        androidx.compose.foundation.Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = stringResource(R.string.cd_qr),
                            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.settings_qr_subtitle), 
                        textAlign = TextAlign.Center, 
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun AparienciaSection(
    configs: Map<String, String>,
    onUpdateConfig: (String, String) -> Unit
) {
    val darkMode = configs["tema_oscuro"] == "true"
    val notifications = configs["notif_activas"] != "false"
    val partnerNotifs = configs["notif_pareja"] != "false"
    val compactView = configs["vista_compacta"] == "true"

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(contentType = "toggle") {
            SettingsToggleItem(
                icon = Icons.Default.DarkMode,
                title = stringResource(R.string.settings_dark_mode_title),
                subtitle = stringResource(R.string.settings_dark_mode_subtitle),
                checked = darkMode,
                onCheckedChange = { onUpdateConfig("tema_oscuro", it.toString()) }
            )
        }
        item(contentType = "toggle") {
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.settings_notifications_title),
                subtitle = stringResource(R.string.settings_notifications_subtitle),
                checked = notifications,
                onCheckedChange = { onUpdateConfig("notif_activas", it.toString()) }
            )
        }
        item(contentType = "toggle") {
            SettingsToggleItem(
                icon = Icons.Default.Groups,
                title = stringResource(R.string.settings_partner_notifications_title),
                subtitle = stringResource(R.string.settings_partner_notifications_desc),
                checked = partnerNotifs,
                onCheckedChange = { onUpdateConfig("notif_pareja", it.toString()) }
            )
        }
        item(contentType = "toggle") {
            SettingsToggleItem(
                icon = Icons.Default.Compress,
                title = stringResource(R.string.settings_compact_view_title),
                subtitle = stringResource(R.string.settings_compact_view_subtitle),
                checked = compactView,
                onCheckedChange = { onUpdateConfig("vista_compacta", it.toString()) }
            )
        }
    }
}

@Composable
fun PreferenciasSection(
    configs: Map<String, String>,
    listas: List<Lista>,
    onUpdateConfig: (String, String) -> Unit
) {
    val currency = configs["moneda"] ?: "€"
    val shopMode = configs["modo_tienda"] == "true"
    val preferredListId = configs["lista_compra_id"]?.toLongOrNull()
    val preferredListName = listas.find { it.id == preferredListId }?.nombre ?: "No seleccionada"

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showListSelector by remember { mutableStateOf(false) }

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

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(contentType = "item") {
            SettingsItem(
                icon = Icons.Default.Payments,
                title = stringResource(R.string.settings_currency_title),
                subtitle = stringResource(R.string.settings_currency_current, currency),
                onClick = { showCurrencyDialog = true }
            )
        }
        item(contentType = "item") {
            SettingsItem(
                icon = Icons.Default.ShoppingCart,
                title = stringResource(R.string.settings_main_list_title),
                subtitle = preferredListName,
                onClick = { showListSelector = true }
            )
        }
        item(contentType = "toggle") {
            SettingsToggleItem(
                icon = Icons.Default.Storefront,
                title = stringResource(R.string.settings_shop_mode_title),
                subtitle = stringResource(R.string.settings_shop_mode_subtitle),
                checked = shopMode,
                onCheckedChange = { onUpdateConfig("modo_tienda", it.toString()) }
            )
        }
    }
}

@Composable
fun CategoryItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    AppCasaCard(onClick = onClick, useGlassmorphism = false) {
        ListItem(
            headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
            leadingContent = { Icon(icon, null, tint = color) },
            trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline) },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
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
  listas: List<Lista>,
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
  enabled: Boolean = true,
  onClick: () -> Unit
) {
  AppCasaCard(
    onClick = if (enabled) onClick else ({}),
    useGlassmorphism = false, // Desactivado por rendimiento en listas largas
    modifier = Modifier.fillMaxWidth().then(if (!enabled) Modifier.alpha(0.5f) else Modifier)
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
  AppCasaCard(
    useGlassmorphism = false, // Desactivado por rendimiento en listas largas
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

@Composable
fun SistemaSection(
    isSyncing: Boolean,
    isExporting: Boolean,
    onForceSync: () -> Unit,
    onExportData: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SettingsItem(
                icon = Icons.Default.Refresh,
                title = "Forzar Sincronización",
                subtitle = if (isSyncing) "Sincronización en curso..." else "Sube todos los datos locales a la nube ahora",
                enabled = !isSyncing,
                onClick = onForceSync
            )
        }
        item {
            SettingsItem(
                icon = Icons.Default.FileUpload,
                title = "Exportar Datos (JSON)",
                subtitle = if (isExporting) "Generando copia..." else "Crea una copia de seguridad externa de tu hogar",
                enabled = !isExporting,
                onClick = onExportData
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
  AppCasaTheme {
    SettingsContent(
      userName = "Juan",
      userAvatar = null,
      householdName = "Villa Casa",
      householdCode = "CASA-1234",
      configs = emptyMap(),
      listas = emptyList(),
      isAdmin = true,
      isSyncing = false,
      isExporting = false,
      onUpdateName = {},
      onUpdateAvatar = {},
      onUpdateHouseholdName = {},
      onUpdateConfig = { _, _ -> },
      onRegenerateCode = {},
      onForceSync = {},
      onExportData = {},
      onLogout = {}
    )
  }
}
