package com.appcasa.features.settings.presentation.screen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onUpdateName = { settingsViewModel.updateUsuario(it) },
    onUpdateAvatar = { imagePickerLauncher.launch("image/*") },
    onUpdateHouseholdName = { settingsViewModel.updateHogar(it) },
    onUpdateConfig = { k, v -> settingsViewModel.updateConfig(k, v) },
    onRegenerateCode = { settingsViewModel.regenerateHouseCode() },
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
  onUpdateName: (String) -> Unit,
  onUpdateAvatar: () -> Unit,
  onUpdateHouseholdName: (String) -> Unit,
  onUpdateConfig: (String, String) -> Unit,
  onRegenerateCode: () -> Unit,
  onLogout: () -> Unit
) {
  var activeSection by remember { mutableStateOf<SettingsSection?>(null) }

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(
      title = { Text(activeSection?.title ?: stringResource(R.string.settings_title)) },
      navigationIcon = {
        if (activeSection != null) {
          IconButton(onClick = { activeSection = null }) {
            Icon(Icons.Default.Close, contentDescription = "Volver")
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
        }
      }
    }
  }
}

enum class SettingsSection(val title: String) {
    HOUSEHOLD("Mi Hogar e Invitaciones"),
    APPEARANCE("Pantalla y Notificaciones"),
    PREFERENCES("Preferencias de Uso")
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
            title = "Tu Nombre",
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
                        Text("Toca para editar nombre o foto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onUpdateAvatar) {
                        Icon(Icons.Default.PhotoCamera, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item(contentType = "header") { SettingsSectionHeader("Gestión") }
        item(contentType = "category") {
            CategoryItem(
                title = "Mi Hogar e Invitaciones",
                subtitle = "Nombre de casa, código QR y pareja",
                icon = Icons.Default.Home,
                onClick = { onSectionClick(SettingsSection.HOUSEHOLD) }
            )
        }

        item(contentType = "header") { SettingsSectionHeader("Personalización") }
        item(contentType = "category") {
            CategoryItem(
                title = "Pantalla y Notificaciones",
                subtitle = "Modo oscuro, notificaciones de pareja...",
                icon = Icons.Default.NotificationsActive,
                onClick = { onSectionClick(SettingsSection.APPEARANCE) }
            )
        }
        item(contentType = "category") {
            CategoryItem(
                title = "Preferencias",
                subtitle = "Moneda, listas y modo tienda",
                icon = Icons.Default.Payments,
                onClick = { onSectionClick(SettingsSection.PREFERENCES) }
            )
        }

        item(contentType = "header") { SettingsSectionHeader("Cuenta") }
        item(contentType = "category") {
            CategoryItem(
                title = "Cerrar Sesión",
                subtitle = "Salir de este perfil familiar",
                icon = Icons.AutoMirrored.Filled.Logout,
                color = MaterialTheme.colorScheme.error,
                onClick = onLogout
            )
        }
        
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                "AppCasa v1.2.0 | Con ❤️ para la familia",
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
            title = "Nombre del Hogar",
            initialValue = householdName,
            onDismiss = { showNameDialog = false },
            onConfirm = { onUpdateName(it); showNameDialog = false }
        )
    }

    if (showRegenerateConfirm) {
        AlertDialog(
            onDismissRequest = { showRegenerateConfirm = false },
            title = { Text("¿Regenerar código?") },
            text = { Text("El código actual dejará de funcionar para nuevas invitaciones.") },
            confirmButton = {
                Button(onClick = { onRegenerateCode(); showRegenerateConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Confirmar")
                }
            },
            dismissButton = { TextButton(onClick = { showRegenerateConfirm = false }) { Text("Cancelar") } }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(contentType = "item") {
            AppCasaCard(onClick = { if (isAdmin) showNameDialog = true }, useGlassmorphism = false) {
                ListItem(
                    headlineContent = { Text("Nombre del Hogar") },
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
                    Text("Código de Invitación", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
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
                            contentDescription = "QR",
                            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Tu pareja puede escanear este código para unirse a la casa al instante.", 
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
                title = "Modo Oscuro",
                subtitle = "Cambiar el tema de la app",
                checked = darkMode,
                onCheckedChange = { onUpdateConfig("tema_oscuro", it.toString()) }
            )
        }
        item(contentType = "toggle") {
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones",
                subtitle = "Recibir avisos de tareas",
                checked = notifications,
                onCheckedChange = { onUpdateConfig("notif_activas", it.toString()) }
            )
        }
        item(contentType = "toggle") {
            SettingsToggleItem(
                icon = Icons.Default.Groups,
                title = "Notificaciones de Pareja",
                subtitle = "Avisar cuando ella haga cambios",
                checked = partnerNotifs,
                onCheckedChange = { onUpdateConfig("notif_pareja", it.toString()) }
            )
        }
        item(contentType = "toggle") {
            SettingsToggleItem(
                icon = Icons.Default.Compress,
                title = "Vista Compacta",
                subtitle = "Listas con menos espacio",
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
                title = "Moneda Local",
                subtitle = "Actual: $currency",
                onClick = { showCurrencyDialog = true }
            )
        }
        item(contentType = "item") {
            SettingsItem(
                icon = Icons.Default.ShoppingCart,
                title = "Lista de la Compra Principal",
                subtitle = preferredListName,
                onClick = { showListSelector = true }
            )
        }
        item(contentType = "toggle") {
            SettingsToggleItem(
                icon = Icons.Default.Storefront,
                title = "Modo Tienda",
                subtitle = "Mantener pantalla encendida",
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
  onClick: () -> Unit
) {
  AppCasaCard(
    onClick = onClick,
    useGlassmorphism = false, // Desactivado por rendimiento en listas largas
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
      onUpdateName = {},
      onUpdateAvatar = {},
      onUpdateHouseholdName = {},
      onUpdateConfig = { _, _ -> },
      onRegenerateCode = {},
      onLogout = {}
    )
  }
}
