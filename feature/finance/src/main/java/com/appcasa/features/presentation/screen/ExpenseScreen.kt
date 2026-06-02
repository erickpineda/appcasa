package com.appcasa.features.finance.presentation.screen

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.data.utils.FileUtils
import com.appcasa.core.ui.components.AppCasaConfirmDialog
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.core.ui.components.AppCasaSutilToast
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.feature.finance.R
import com.appcasa.features.finance.data.local.ExpenseEntity
import com.appcasa.features.finance.presentation.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
  navController: NavController,
  viewModel: FinanceViewModel = hiltViewModel()
) {
  val expenses by viewModel.expenses.collectAsState()
  val currency by viewModel.currencySymbol.collectAsState()
  val ocrResult by viewModel.ocrResult.collectAsState()
  val ocrStore by viewModel.ocrStore.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }
  var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
  var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
  val context = LocalContext.current
  var toastMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    viewModel.toastEvent.collect { message ->
        toastMessage = message
    }
  }

  AppCasaConfirmDialog(
    show = expenseToDelete != null,
    title = stringResource(R.string.finance_delete_title),
    text = stringResource(R.string.finance_delete_confirm, expenseToDelete?.concepto ?: ""),
    onConfirm = {
        expenseToDelete?.let { viewModel.archiveExpense(it) }
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
      initialConcepto = ocrStore ?: "",
      onDismiss = { 
          showAddDialog = false
          viewModel.clearOcr()
      },
      onConfirm = { concepto, importe, categoria, fotoUri ->
        viewModel.addExpense(concepto, importe, categoria, fotoUri)
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
      onConfirm = { concepto, importe, categoria, fotoUri ->
        viewModel.updateExpense(expense.copy(
            concepto = concepto,
            importe = importe,
            categoria = categoria,
            fotoUri = fotoUri
        ))
        editingExpense = null
      }
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
      PullToRefreshWrapper {
        Scaffold(
          topBar = {
            TopAppBar(
              title = { Text(stringResource(R.string.finance_title)) },
              navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                  Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = MaterialTheme.colorScheme.onPrimary)
                }
              },
              colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
              ),
              actions = {
                IconButton(onClick = { navController.navigate(com.appcasa.navigation.Screen.FinanceStats.route) }) {
                    Icon(Icons.Default.Assessment, contentDescription = "Estadísticas")
                }
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = stringResource(R.string.cd_scan_ticket))
                }
                if (expenses.isNotEmpty()) {
                  val shareSummaryTitle = stringResource(R.string.finance_share_summary_title)
                  val shareTotalLabel = stringResource(R.string.finance_share_total_label)
                  IconButton(onClick = {
                    val total = expenses.sumOf { it.importe }
                    val shareText = shareSummaryTitle + "\n" + 
                      expenses.joinToString("\n") { "- ${it.concepto}: ${String.format("%.2f", it.importe)} $currency" } +
                      String.format(shareTotalLabel, String.format("%.2f", total), currency)
                    
                    val sendIntent: Intent = Intent().apply {
                      action = Intent.ACTION_SEND
                      putExtra(Intent.EXTRA_TEXT, shareText)
                      type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                  }) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.cd_share))
                  }
                }
              }
            )
          },
          floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
              Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_new_expense))
            }
          },
          contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                  title = stringResource(R.string.finance_empty_title),
                  description = stringResource(R.string.finance_empty_description),
                  icon = Icons.Default.ReceiptLong,
                  actionText = stringResource(R.string.finance_add_action),
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
    
              item {
                TextButton(
                    onClick = { viewModel.loadMoreActive() }, 
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cargar más gastos antiguos...")
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
fun ExpenseCard(expense: ExpenseEntity, currency: String, onEdit: () -> Unit, onDelete: () -> Unit) {
  var showImagePreview by remember { mutableStateOf(false) }

  if (showImagePreview && expense.fotoUri != null) {
      AlertDialog(
          onDismissRequest = { showImagePreview = false },
          text = {
              AsyncImage(
                  model = expense.fotoUri,
                  contentDescription = null,
                  modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                  contentScale = ContentScale.Fit
              )
          },
          confirmButton = {
              TextButton(onClick = { showImagePreview = false }) { Text("Cerrar") }
          }
      )
  }

  com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true,
    modifier = Modifier.fillMaxWidth()
  ) {
    ListItem(
      headlineContent = { Text(expense.concepto) },
      supportingContent = { Text("${expense.categoria} · ${formatDate(expense.fecha)}") },
      leadingContent = {
          if (expense.fotoUri != null) {
              AsyncImage(
                  model = expense.fotoUri,
                  contentDescription = null,
                  modifier = Modifier
                      .size(48.dp)
                      .clip(RoundedCornerShape(4.dp))
                      .clickable { showImagePreview = true },
                  contentScale = ContentScale.Crop
              )
          } else {
              Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          }
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
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
          }
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
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
  initialConcepto: String = "",
  onDismiss: () -> Unit,
  onConfirm: (String, Double, String, String?) -> Unit
) {
  var concepto by remember { mutableStateOf(if (initialConcepto.isNotEmpty()) initialConcepto else item?.concepto ?: "") }
  var importe by remember { mutableStateOf(if (initialImporte.isNotEmpty()) initialImporte else item?.importe?.toString() ?: "") }
  var categoria by remember { mutableStateOf(item?.categoria ?: "Otros") }
  var fotoUri by remember { mutableStateOf(item?.fotoUri) }
  
  val context = LocalContext.current
  val imagePickerLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
  ) { uri ->
      uri?.let {
          fotoUri = FileUtils.saveImageLocally(context, it.toString())
      }
  }

  val isImporteValid = remember(importe) { 
    importe.toDoubleOrNull()?.let { it > 0 } ?: false 
  }
  val canConfirm = concepto.isNotBlank() && isImporteValid

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(if (item == null) R.string.finance_action_add_title else R.string.finance_action_edit_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (initialImporte.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    stringResource(R.string.finance_ocr_detected, initialImporte, currency),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        OutlinedTextField(
          value = concepto, 
          onValueChange = { concepto = it }, 
          label = { Text(stringResource(R.string.finance_label_concept)) }, 
          modifier = Modifier.fillMaxWidth(),
          isError = concepto.isEmpty(),
          singleLine = true,
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        OutlinedTextField(
          value = importe, 
          onValueChange = { 
              if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' || char == ',' }) {
                  importe = it.replace(',', '.')
              }
          }, 
          label = { Text(stringResource(R.string.finance_label_amount, currency)) },
          keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next
          ),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          isError = importe.isNotEmpty() && !isImporteValid,
          supportingText = {
            if (importe.isNotEmpty() && !isImporteValid) {
              Text(stringResource(R.string.finance_error_amount_invalid), color = MaterialTheme.colorScheme.error)
            }
          }
        )
        OutlinedTextField(
            value = categoria, 
            onValueChange = { categoria = it }, 
            label = { Text(stringResource(R.string.finance_label_category)) }, 
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Spacer(Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (fotoUri == null) "Adjuntar Ticket" else "Cambiar Foto", style = MaterialTheme.typography.labelSmall)
            }
            
            if (fotoUri != null) {
                AsyncImage(
                    model = fotoUri,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                IconButton(onClick = { fotoUri = null }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { 
          onConfirm(concepto, importe.toDouble(), categoria, fotoUri)
        },
        enabled = canConfirm
      ) {
        Text(stringResource(if (item == null) R.string.finance_btn_save else R.string.finance_btn_update))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.finance_btn_cancel)) }
    }
  )
}

private fun formatDate(timestamp: Long): String {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
