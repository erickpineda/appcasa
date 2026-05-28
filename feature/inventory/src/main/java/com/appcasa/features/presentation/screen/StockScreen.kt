package com.appcasa.features.inventory.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.features.inventory.data.local.StockEntity
import com.appcasa.features.inventory.presentation.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
  navController: NavController,
  viewModel: StockViewModel = hiltViewModel()
) {
  val stockItems by viewModel.stockItems.collectAsState()
  val isCompact by viewModel.isCompactView.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }
  var editingItem by remember { mutableStateOf<StockEntity?>(null) }

  if (showAddDialog) {
    StockActionDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { nombre, categoria, cantidad, minima, unidad ->
        viewModel.addItem(nombre, categoria, cantidad, minima, unidad)
        showAddDialog = false
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

  PullToRefreshWrapper {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text("Inventario y Stock") },
          navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
          }
        )
      },
      floatingActionButton = {
        FloatingActionButton(onClick = { showAddDialog = true }) {
          Icon(Icons.Default.Add, contentDescription = "Añadir Artículo")
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
            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
              Text("No hay artículos en el inventario")
            }
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
              onAddToList = { viewModel.addToShoppingList(item.nombre) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun StockActionDialog(
  item: StockEntity? = null,
  onDismiss: () -> Unit,
  onConfirm: (String, String, Double, Double, String) -> Unit
) {
  var nombre by remember { mutableStateOf(item?.nombre ?: "") }
  var categoria by remember { mutableStateOf(item?.categoria ?: "Despensa") }
  var cantidad by remember { mutableStateOf(item?.cantidadActual?.toString() ?: "") }
  var minima by remember { mutableStateOf(item?.cantidadMinima?.toString() ?: "") }
  var unidad by remember { mutableStateOf(item?.unidad ?: "uds") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (item == null) "Nuevo Artículo" else "Editar Artículo") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = cantidad, 
            onValueChange = { cantidad = it }, 
            label = { Text("Cant.") }, 
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
          OutlinedTextField(
            value = minima, 
            onValueChange = { minima = it }, 
            label = { Text("Mín.") }, 
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
          )
        }
        OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text("Unidad (kg, sacos...)") }, modifier = Modifier.fillMaxWidth())
      }
    },
    confirmButton = {
      Button(onClick = { 
        if (nombre.isNotBlank()) {
          onConfirm(nombre, categoria, cantidad.toDoubleOrNull() ?: 0.0, minima.toDoubleOrNull() ?: 0.0, unidad)
        }
      }) {
        Text(if (item == null) "Añadir" else "Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
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
            text = if (isLowStock) "STOCK BAJO • ${item.categoria}" else item.categoria, 
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
          Text("AÑADIR A LA COMPRA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
