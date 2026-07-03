package com.appcasa.features.inventory.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.appcasa.core.utils.Constants
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.model.TipoLista
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.AppCasaSutilToast
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.feature.inventory.R
import com.appcasa.features.inventory.presentation.viewmodel.StockViewModel
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import com.appcasa.core.ui.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
  navController: NavController,
  viewModel: StockViewModel = hiltViewModel()
) {
  val stockItems by viewModel.stockItems.collectAsStateWithLifecycle()
  val availableLists by viewModel.availableLists.collectAsStateWithLifecycle()
  val isCompact by viewModel.isCompactView.collectAsStateWithLifecycle()
  val barcodeResult by viewModel.barcodeResult.collectAsStateWithLifecycle()
  
  var showAddDialog by remember { mutableStateOf(false) }
  var editingItem by remember { mutableStateOf<StockItem?>(null) }
  var itemToAddToList by remember { mutableStateOf<StockItem?>(null) }
  var itemToDelete by remember { mutableStateOf<StockItem?>(null) }
  val context = LocalContext.current
  var toastMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    viewModel.toastEvent.collect { message ->
      toastMessage = message
    }
  }

  AppCasaConfirmDialog(
    show = itemToDelete != null,
    title = stringResource(R.string.inventory_delete_title),
    text = stringResource(R.string.inventory_delete_confirm),
    onConfirm = {
      itemToDelete?.let { viewModel.deleteItem(it) }
      itemToDelete = null
    },
    onDismiss = { itemToDelete = null }
  )

  val barcodeLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      val image = InputImage.fromFilePath(context, it)
      viewModel.scanBarcode(image)
      showAddDialog = true
    }
  }

  if (showAddDialog) {
    StockActionDialog(
      initialBarcode = barcodeResult ?: "",
      onDismiss = { 
        showAddDialog = false
        viewModel.clearBarcode()
      },
      onConfirm = { nombre, categoria, cantidad, minima, unidad ->
        viewModel.addItem(nombre, categoria, cantidad, minima, unidad)
        showAddDialog = false
        viewModel.clearBarcode()
      }
    )
  }

  editingItem?.let { item ->
    StockActionDialog(
      item = item,
      onDismiss = { editingItem = null },
      onConfirm = { nombre, categoria, cantidad, minima, unidad ->
        viewModel.upsertItem(item.copy(
          nombre = nombre,
          categoria = categoria,
          cantidadActual = cantidad,
          cantidadMinima = minima,
          unidad = unidad
        ))
        editingItem = null
      }
    )
  }

  itemToAddToList?.let { item ->
    AddToListDialog(
      item = item,
      lists = availableLists,
      onDismiss = { itemToAddToList = null },
      onConfirm = { listId, quantity ->
        viewModel.addToShoppingList(item, listId, quantity)
        itemToAddToList = null
      }
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    PullToRefreshWrapper {
      Scaffold(
        topBar = {
          TopAppBar(
            title = { Text(stringResource(R.string.inventory_title)) },
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back), tint = MaterialTheme.colorScheme.onPrimary)
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.primary,
              titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            actions = {
              IconButton(onClick = { barcodeLauncher.launch(Constants.Media.MIME_TYPE_IMAGE) }) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = stringResource(R.string.cd_scan))
              }
            }
          )
        },
        floatingActionButton = {
          FloatingActionButton(onClick = { showAddDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_item))
          }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
      ) { padding ->
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(padding),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 12.dp)
        ) {
          if (stockItems.isEmpty()) {
            item {
              AppCasaEmptyState(
                title = stringResource(R.string.inventory_empty_title),
                description = stringResource(R.string.inventory_empty_description),
                icon = Icons.Default.Inventory,
                actionText = stringResource(R.string.inventory_add_action),
                onActionClick = { showAddDialog = true },
                modifier = Modifier.fillParentMaxSize()
              )
            }
          } else {
            items(stockItems) { item ->
              StockItemCard(
                item = item,
                isCompact = isCompact,
                onAdd = { viewModel.updateQuantity(item, 1.0) },
                onRemove = { viewModel.updateQuantity(item, -1.0) },
                onEdit = { editingItem = item },
                onDelete = { itemToDelete = item },
                onAddToList = { itemToAddToList = item }
              )
            }
    
            item {
              TextButton(
                onClick = { viewModel.loadMore() }, 
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(stringResource(R.string.inventory_load_more))
              }
            }
          }
            
          item {
            Spacer(Modifier.imePadding())
          }
        }
      }
    }

    AppCasaSutilToast(
      message = toastMessage,
      onDismiss = { toastMessage = null }
    )
  }
}

@Composable
fun AddToListDialog(
  item: StockItem,
  lists: List<Lista>,
  onDismiss: () -> Unit,
  onConfirm: (String, Double) -> Unit
) {
  val initialMissing = (item.cantidadMinima - item.cantidadActual).coerceAtLeast(1.0)
  var quantity by remember { mutableStateOf(initialMissing.toString()) }
  var selectedListId by remember { mutableStateOf<String?>(lists.find { it.tipo == TipoLista.COMPRA }?.id ?: lists.firstOrNull()?.id) }
  var expanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.inventory_add_to_list_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.inventory_add_to_list_question, item.nombre), style = MaterialTheme.typography.bodyMedium)
        
        OutlinedTextField(
          value = quantity,
          onValueChange = { quantity = it },
          label = { Text(stringResource(R.string.inventory_label_quantity_unit, item.unidad)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        if (lists.isEmpty()) {
          Text(stringResource(R.string.inventory_no_lists_error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        } else {
          Text(stringResource(R.string.inventory_select_list), style = MaterialTheme.typography.labelSmall)
          Box {
            OutlinedButton(
              onClick = { expanded = true },
              modifier = Modifier.fillMaxWidth()
            ) {
              val selectedName = lists.find { it.id == selectedListId }?.nombre ?: stringResource(R.string.inventory_select_list)
              Text(selectedName)
              Spacer(Modifier.weight(1f))
              Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
              lists.forEach { list ->
                DropdownMenuItem(
                  text = { Text(list.nombre) },
                  onClick = {
                    selectedListId = list.id
                    expanded = false
                  }
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { 
          val q = quantity.toDoubleOrNull() ?: 1.0
          selectedListId?.let { onConfirm(it, q) }
        },
        enabled = selectedListId != null && quantity.toDoubleOrNull() != null
      ) {
        Text(stringResource(R.string.inventory_btn_add))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(CoreR.string.common_cancel)) }
    }
  )
}

@Composable
fun StockActionDialog(
  item: StockItem? = null,
  initialBarcode: String = "",
  onDismiss: () -> Unit,
  onConfirm: (String, String, Double, Double, String) -> Unit
) {
  val pantryCat = stringResource(R.string.inventory_cat_pantry)
  val unitsDefault = stringResource(R.string.inventory_unit_default)
  val barcodeNamePlaceholder = stringResource(R.string.inventory_placeholder_barcode_name, initialBarcode)

  var nombre by remember { mutableStateOf(if (initialBarcode.isNotEmpty()) barcodeNamePlaceholder else item?.nombre ?: "") }
  var nombreTouched by remember { mutableStateOf(false) }
  var categoria by remember { mutableStateOf(item?.categoria ?: pantryCat) }
  var cantidad by remember { mutableStateOf(item?.cantidadActual?.toString() ?: "") }
  var minima by remember { mutableStateOf(item?.cantidadMinima?.toString() ?: "") }
  var unidad by remember { mutableStateOf(item?.unidad ?: unitsDefault) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    delay(300)
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  val isNombreValid = nombre.isNotBlank()
  val canConfirm = isNombreValid

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(if (item == null) R.string.inventory_new_item_title else R.string.inventory_edit_item_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (initialBarcode.isNotEmpty()) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
          ) {
            Text(
              stringResource(R.string.inventory_barcode_detected, initialBarcode),
              modifier = Modifier.padding(8.dp),
              style = MaterialTheme.typography.labelSmall
            )
          }
        }
        OutlinedTextField(
          value = nombre, 
          onValueChange = { 
            nombre = it
            nombreTouched = true
          }, 
          label = { Text(stringResource(R.string.inventory_label_name)) }, 
          modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
          isError = nombreTouched && !isNombreValid,
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
          supportingText = {
            if (nombreTouched && !isNombreValid) {
              Text(stringResource(R.string.inventory_error_name_required), color = MaterialTheme.colorScheme.error)
            }
          }
        )
        OutlinedTextField(
          value = categoria, 
          onValueChange = { categoria = it }, 
          label = { Text(stringResource(R.string.inventory_label_category)) }, 
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = cantidad, 
            onValueChange = { cantidad = it }, 
            label = { Text(stringResource(R.string.inventory_label_qty_short)) }, 
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
          OutlinedTextField(
            value = minima, 
            onValueChange = { minima = it }, 
            label = { Text(stringResource(R.string.inventory_label_min_short)) }, 
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
        }
        OutlinedTextField(
          value = unidad, 
          onValueChange = { unidad = it }, 
          label = { Text(stringResource(R.string.inventory_label_unit)) }, 
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { 
          onConfirm(nombre, categoria, cantidad.toDoubleOrNull() ?: 0.0, minima.toDoubleOrNull() ?: 0.0, unidad)
        },
        enabled = canConfirm
      ) {
        Text(stringResource(if (item == null) R.string.inventory_btn_add else R.string.inventory_btn_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(CoreR.string.common_cancel)) }
    }
  )
}

@Composable
fun StockItemCard(
  item: StockItem,
  isCompact: Boolean,
  onAdd: () -> Unit,
  onRemove: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onAddToList: () -> Unit
) {
  val isLowStock = item.cantidadActual <= item.cantidadMinima
  
  AppCasaCard(
    useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth(),
    containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f) else null
  ) {
    Column(
      modifier = Modifier.padding(horizontal = if (isCompact) 12.dp else 16.dp, vertical = if (isCompact) 8.dp else 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Título y Categoría
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = item.nombre, 
              style = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium, 
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false)
            )
            if (isLowStock) {
              Icon(
                Icons.Default.Warning, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp).size(14.dp)
              )
            }
          }
          Text(
            text = if (isLowStock) stringResource(R.string.inventory_low_stock_label, item.categoria) else item.categoria, 
            style = MaterialTheme.typography.labelSmall,
            color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = if (isLowStock) FontWeight.ExtraBold else FontWeight.Normal
          )
        }

        // Botones de acción alineados a la derecha
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onRemove, modifier = Modifier.size(if (isCompact) 28.dp else 36.dp)) {
            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
          }
          Text(
            text = "${if (item.cantidadActual % 1 == 0.0) item.cantidadActual.hashCode() else item.cantidadActual} ${item.unidad}",
            style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 4.dp)
          )
          IconButton(onClick = onAdd, modifier = Modifier.size(if (isCompact) 28.dp else 36.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          }
          
          Spacer(modifier = Modifier.width(4.dp))
          
          IconButton(onClick = onEdit, modifier = Modifier.size(if (isCompact) 28.dp else 32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(if (isCompact) 28.dp else 32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
          }
        }
      }

      // Botón "Comprar" solo si hay stock bajo, en una línea limpia
      if (isLowStock) {
        Button(
          onClick = onAddToList,
          modifier = Modifier.padding(top = 8.dp).height(28.dp).fillMaxWidth(),
          contentPadding = PaddingValues(0.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.primary
          ),
          elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
          Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(12.dp))
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.inventory_add_to_shopping_btn), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
