package com.appcasa.features.inventory.presentation.screen

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.features.inventory.data.local.StockEntity
import com.appcasa.features.inventory.presentation.viewmodel.StockViewModel
import androidx.compose.ui.res.stringResource
import com.google.mlkit.vision.common.InputImage
import androidx.compose.ui.res.stringResource
import com.appcasa.feature.inventory.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
  navController: NavController,
  viewModel: StockViewModel = hiltViewModel()
) {
  val stockItems by viewModel.stockItems.collectAsState()
  val availableLists by viewModel.availableLists.collectAsState()
  val isCompact by viewModel.isCompactView.collectAsState()
  val barcodeResult by viewModel.barcodeResult.collectAsState()
  
  var showAddDialog by remember { mutableStateOf(false) }
  var editingItem by remember { mutableStateOf<StockEntity?>(null) }
  var itemToAddToList by remember { mutableStateOf<StockEntity?>(null) }
  val context = LocalContext.current

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
        viewModel.updateItem(item.copy(
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

  PullToRefreshWrapper {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text(stringResource(R.string.inventory_title)) },
          navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = MaterialTheme.colorScheme.onPrimary)
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
          ),
          actions = {
              IconButton(onClick = { barcodeLauncher.launch("image/*") }) {
                  Icon(Icons.Default.QrCodeScanner, contentDescription = stringResource(R.string.cd_scan))
              }
          }
        )
      },
      floatingActionButton = {
        FloatingActionButton(onClick = { showAddDialog = true }) {
          Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_item))
        }
      }
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
              onDelete = { viewModel.deleteItem(item) },
              onAddToList = { itemToAddToList = item }
            )
          }
        }
      }
    }
  }
}

@Composable
fun AddToListDialog(
  item: StockEntity,
  lists: List<com.appcasa.features.lists.data.local.ListaEntity>,
  onDismiss: () -> Unit,
  onConfirm: (Long, Double) -> Unit
) {
  val initialMissing = (item.cantidadMinima - item.cantidadActual).coerceAtLeast(1.0)
  var quantity by remember { mutableStateOf(initialMissing.toString()) }
  var selectedListId by remember { mutableStateOf<Long?>(lists.find { it.tipo == com.appcasa.core.domain.model.TipoLista.COMPRA.name }?.id ?: lists.firstOrNull()?.id) }
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
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.inventory_btn_cancel)) }
    }
  )
}

@Composable
fun StockActionDialog(
  item: StockEntity? = null,
  initialBarcode: String = "",
  onDismiss: () -> Unit,
  onConfirm: (String, String, Double, Double, String) -> Unit
) {
  var nombre by remember { mutableStateOf(if (initialBarcode.isNotEmpty()) "Producto $initialBarcode" else item?.nombre ?: "") }
  var nombreTouched by remember { mutableStateOf(false) }
  var categoria by remember { mutableStateOf(item?.categoria ?: "Despensa") }
  var cantidad by remember { mutableStateOf(item?.cantidadActual?.toString() ?: "") }
  var minima by remember { mutableStateOf(item?.cantidadMinima?.toString() ?: "") }
  var unidad by remember { mutableStateOf(item?.unidad ?: "uds") }

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
            modifier = Modifier.fillMaxWidth(),
            isError = nombreTouched && !isNombreValid,
            supportingText = {
                if (nombreTouched && !isNombreValid) {
                    Text(stringResource(R.string.inventory_error_name_required), color = MaterialTheme.colorScheme.error)
                }
            }
        )
        OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text(stringResource(R.string.inventory_label_category)) }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = cantidad, 
            onValueChange = { cantidad = it }, 
            label = { Text(stringResource(R.string.inventory_label_qty_short)) }, 
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
          OutlinedTextField(
            value = minima, 
            onValueChange = { minima = it }, 
            label = { Text(stringResource(R.string.inventory_label_min_short)) }, 
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
        }
        OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text(stringResource(R.string.inventory_label_unit)) }, modifier = Modifier.fillMaxWidth())
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
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.inventory_btn_cancel)) }
    }
  )
}

@Composable
fun StockItemCard(
  item: StockEntity,
  isCompact: Boolean,
  onAdd: () -> Unit,
  onRemove: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onAddToList: () -> Unit
) {
  val isLowStock = item.cantidadActual <= item.cantidadMinima
  
  com.appcasa.core.ui.components.AppCasaCard(
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
            text = "${if (item.cantidadActual % 1 == 0.0) item.cantidadActual.toInt() else item.cantidadActual} ${item.unidad}",
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
