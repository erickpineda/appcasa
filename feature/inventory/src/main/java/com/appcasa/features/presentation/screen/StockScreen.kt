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
import androidx.compose.ui.unit.dp
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

  if (showAddDialog) {
    AddStockDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { nombre, categoria, cantidad, minima, unidad ->
        viewModel.addItem(nombre, categoria, cantidad, minima, unidad)
        showAddDialog = false
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
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 12.dp)
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
fun AddStockDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, Double, Double, String) -> Unit
) {
  var nombre by remember { mutableStateOf("") }
  var categoria by remember { mutableStateOf("Despensa") }
  var cantidad by remember { mutableStateOf("") }
  var minima by remember { mutableStateOf("") }
  var unidad by remember { mutableStateOf("uds") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Nuevo Artículo") },
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
        Text("Añadir")
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
  onDelete: () -> Unit,
  onAddToList: () -> Unit
) {
  val isLowStock = item.cantidadActual <= item.cantidadMinima
  
  com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth().alpha(if (isLowStock) 0.6f else 1f)
  ) {
    Row(
      modifier = Modifier.padding(if (isCompact) 4.dp else 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.nombre, 
          style = if (isCompact) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium, 
          fontWeight = FontWeight.Bold
        )
        if (!isCompact) {
          Text(text = item.categoria, style = MaterialTheme.typography.bodySmall)
        }
        if (isLowStock) {
          Text(
            text = "¡Stock Bajo!",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (isCompact) 0.dp else 4.dp)) {
        if (isLowStock) {
          IconButton(onClick = onAddToList) {
            Icon(
              imageVector = Icons.Default.ShoppingCart, 
              contentDescription = "Añadir a la compra", 
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
            )
          }
        }
        
        IconButton(onClick = onRemove) {
          Icon(
            imageVector = Icons.Default.Remove, 
            contentDescription = "Quitar",
            modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
          )
        }
        Text(
          text = "${item.cantidadActual} ${item.unidad}",
          style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 4.dp)
        )
        IconButton(onClick = onAdd) {
          Icon(
            imageVector = Icons.Default.Add, 
            contentDescription = "Añadir",
            modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
          )
        }
        IconButton(onClick = onDelete) {
          Icon(
            Icons.Default.Delete, 
            contentDescription = "Borrar", 
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
            modifier = Modifier.size(if (isCompact) 18.dp else 24.dp)
          )
        }
      }
    }
  }
}
