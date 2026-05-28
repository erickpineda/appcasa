package com.appcasa.features.finance.presentation.screen

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
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
  var showAddDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current

  if (showAddDialog) {
    AddExpenseDialog(
      currency = currency,
      onDismiss = { showAddDialog = false },
      onConfirm = { concepto, importe, categoria ->
        viewModel.addExpense(concepto, importe, categoria)
        showAddDialog = false
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
              Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
          },
          actions = {
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
            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
              Text("No hay gastos registrados")
            }
          }
        } else {
          items(expenses) { expense ->
            ExpenseCard(
              expense = expense,
              currency = currency,
              onDelete = { viewModel.deleteExpense(expense) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun ExpenseCard(expense: ExpenseEntity, currency: String, onDelete: () -> Unit) {
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
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
          }
        }
      }
    )
  }
}

@Composable
fun AddExpenseDialog(
  currency: String,
  onDismiss: () -> Unit,
  onConfirm: (String, Double, String) -> Unit
) {
  var concepto by remember { mutableStateOf("") }
  var importe by remember { mutableStateOf("") }
  var categoria by remember { mutableStateOf("Otros") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Registrar Gasto") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
          value = importe, 
          onValueChange = { importe = it }, 
          label = { Text("Importe ($currency)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
      }
    },
    confirmButton = {
      Button(onClick = { 
        val valImporte = importe.toDoubleOrNull() ?: 0.0
        if (concepto.isNotBlank() && valImporte > 0) {
          onConfirm(concepto, valImporte, categoria)
        }
      }) {
        Text("Guardar")
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
