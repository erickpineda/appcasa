package com.appcasa.features.finance.presentation.screen

import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.features.finance.data.local.ExpenseEntity
import com.appcasa.features.finance.presentation.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
  navController: NavController,
  viewModel: FinanceViewModel = hiltViewModel()
) {
  val expenses by viewModel.expenses.collectAsState()
  val currency by viewModel.currencySymbol.collectAsState()
  val ocrResult by viewModel.ocrResult.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }
  var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
  var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
  val context = LocalContext.current

  AppCasaConfirmDialog(
    show = expenseToDelete != null,
    title = "Borrar Gasto",
    text = "¿Estás seguro de que quieres eliminar '${expenseToDelete?.concepto}'? Esta acción no se puede deshacer.",
    onConfirm = {
        expenseToDelete?.let { viewModel.deleteExpense(it) }
        expenseToDelete = null
    },
    onDismiss = { expenseToDelete = null }
  )

  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      val bitmap = if (Build.VERSION.SDK_INT < 28) {
          MediaStore.Images.Media.getBitmap(context.contentResolver, it)
      } else {
          val source = ImageDecoder.createSource(context.contentResolver, it)
          ImageDecoder.decodeBitmap(source)
      }
      viewModel.processTicket(bitmap)
      showAddDialog = true
    }
  }

  if (showAddDialog) {
    ExpenseActionDialog(
      currency = currency,
      initialImporte = ocrResult?.toString() ?: "",
      onDismiss = { 
          showAddDialog = false
          viewModel.clearOcr()
      },
      onConfirm = { concepto, importe, categoria ->
        viewModel.addExpense(concepto, importe, categoria)
        showAddDialog = false
        viewModel.clearOcr()
      }
    )
  }

  editingExpense?.let { expense ->
    ExpenseActionDialog(
      item = expense,
      currency = currency,
      onDismiss = { editingExpense = null },
      onConfirm = { concepto, importe, categoria ->
        viewModel.updateExpense(expense.copy(
            concepto = concepto,
            importe = importe,
            categoria = categoria
        ))
        editingExpense = null
      }
    )
  }

  PullToRefreshWrapper {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text("Gastos del Hogar") },
          navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onPrimary)
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
          ),
          actions = {
            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                Icon(Icons.Default.DocumentScanner, contentDescription = "Escanear ticket")
            }
            if (expenses.isNotEmpty()) {
              IconButton(onClick = {
                val total = expenses.sumOf { it.importe }
                val shareText = "💰 *Resumen de Gastos AppCasa*:\n" + 
                  expenses.joinToString("\n") { "- ${it.concepto}: ${String.format("%.2f", it.importe)} $currency" } +
                  "\n\n*TOTAL: ${String.format("%.2f", total)} $currency*"
                
                val sendIntent: Intent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, shareText)
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
              }) {
                Icon(Icons.Default.Share, contentDescription = "Compartir gastos")
              }
            }
          }
        )
      },
      floatingActionButton = {
        FloatingActionButton(onClick = { showAddDialog = true }) {
          Icon(Icons.Default.Add, contentDescription = "Nuevo Gasto")
        }
      }
    ) { padding ->
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (expenses.isEmpty()) {
          item {
            AppCasaEmptyState(
              title = "Sin gastos aún",
              description = "Anota tu primer gasto para empezar a llevar el control del hogar. ¡Puedes escanear tickets!",
              icon = Icons.Default.ReceiptLong,
              actionText = "Añadir gasto",
              onActionClick = { showAddDialog = true },
              modifier = Modifier.fillParentMaxSize()
            )
          }
        } else {
          items(expenses) { expense ->
            ExpenseCard(
              expense = expense,
              currency = currency,
              onEdit = { editingExpense = expense },
              onDelete = { expenseToDelete = expense }
            )
          }
        }
      }
    }
  }
}

@Composable
fun ExpenseCard(expense: ExpenseEntity, currency: String, onEdit: () -> Unit, onDelete: () -> Unit) {
  com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth()
  ) {
    ListItem(
      headlineContent = { Text(expense.concepto) },
      supportingContent = { Text("${expense.categoria} · ${formatDate(expense.fecha)}") },
      leadingContent = {
        Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      },
      trailingContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${String.format("%.2f", expense.importe)} $currency",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
          )
          IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
          }
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
          }
        }
      }
    )
  }
}

@Composable
fun ExpenseActionDialog(
  item: ExpenseEntity? = null,
  currency: String,
  initialImporte: String = "",
  onDismiss: () -> Unit,
  onConfirm: (String, Double, String) -> Unit
) {
  var concepto by remember { mutableStateOf(item?.concepto ?: "") }
  var importe by remember { mutableStateOf(if (initialImporte.isNotEmpty()) initialImporte else item?.importe?.toString() ?: "") }
  var categoria by remember { mutableStateOf(item?.categoria ?: "Otros") }
  
  val isImporteValid = remember(importe) { 
    importe.toDoubleOrNull()?.let { it > 0 } ?: false 
  }
  val canConfirm = concepto.isNotBlank() && isImporteValid

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (item == null) "Registrar Gasto" else "Editar Gasto") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (initialImporte.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    "Ticket escaneado: $initialImporte $currency",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        OutlinedTextField(
          value = concepto, 
          onValueChange = { concepto = it }, 
          label = { Text("Concepto") }, 
          modifier = Modifier.fillMaxWidth(),
          isError = concepto.isEmpty()
        )
        OutlinedTextField(
          value = importe, 
          onValueChange = { importe = it }, 
          label = { Text("Importe ($currency)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          isError = importe.isNotEmpty() && !isImporteValid,
          supportingText = {
            if (importe.isNotEmpty() && !isImporteValid) {
              Text("El importe debe ser un número mayor a 0", color = MaterialTheme.colorScheme.error)
            }
          }
        )
        OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
      }
    },
    confirmButton = {
      Button(
        onClick = { 
          onConfirm(concepto, importe.toDouble(), categoria)
        },
        enabled = canConfirm
      ) {
        Text(if (item == null) "Guardar" else "Actualizar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    }
  )
}

private fun formatDate(timestamp: Long): String {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
