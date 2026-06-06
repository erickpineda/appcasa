package com.appcasa.features.pets.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.PetDeworming
import com.appcasa.core.domain.model.PetMedication
import com.appcasa.core.domain.model.PetWeight
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.components.skeletonShimmer
import com.appcasa.features.family.R
import com.appcasa.features.pets.presentation.viewmodel.PetDetailViewModel
import com.appcasa.navigation.Screen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
  navController: NavController,
  viewModel: PetDetailViewModel = hiltViewModel()
) {
  val pet by viewModel.pet.collectAsState()
  val pesos by viewModel.pesos.collectAsState()
  val vacunas by viewModel.vacunas.collectAsState()
  val medicaciones by viewModel.medicaciones.collectAsState()
  val desparasitaciones by viewModel.desparasitaciones.collectAsState()
  val scrollState = rememberScrollState()

  var showWeightDialog by remember { mutableStateOf(false) }
  var showMedicationDialog by remember { mutableStateOf(false) }
  var showVaccineDialog by remember { mutableStateOf(false) }
  var showDewormingDialog by remember { mutableStateOf(false) }
  
  var editingMedication by remember { mutableStateOf<PetMedication?>(null) }

  if (showWeightDialog) {
    WeightDialog(
      onDismiss = { showWeightDialog = false },
      onConfirm = { weight ->
        viewModel.addPeso(weight)
        showWeightDialog = false
      }
    )
  }

  if (showMedicationDialog) {
    MedicationActionDialog(
      onDismiss = { showMedicationDialog = false },
      onConfirm = { nombre, dosis, frecuencia ->
        viewModel.addMedicacion(nombre, dosis, frecuencia)
        showMedicationDialog = false
      }
    )
  }

  editingMedication?.let { med ->
    MedicationActionDialog(
      item = med,
      onDismiss = { editingMedication = null },
      onConfirm = { nombre, dosis, frecuencia ->
        viewModel.updateMedicacion(med.copy(nombre = nombre, dosis = dosis, frecuencia = frecuencia))
        editingMedication = null
      }
    )
  }

  if (showVaccineDialog) {
    VaccineDialog(
      onDismiss = { showVaccineDialog = false },
      onConfirm = { nombre ->
        viewModel.addVacuna(nombre)
        showVaccineDialog = false
      }
    )
  }

  if (showDewormingDialog) {
    DewormingDialog(
      onDismiss = { showDewormingDialog = false },
      onConfirm = { tipo, producto ->
        viewModel.addDesparasitacion(tipo, producto)
        showDewormingDialog = false
      }
    )
  }

  AppCasaMeshBackground {
    PullToRefreshWrapper {
      Scaffold(
        topBar = {
          TopAppBar(
            title = { Text(pet?.nombre ?: stringResource(R.string.dashboard_pet_fallback)) },
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
              }
            },
            actions = {
              IconButton(onClick = { pet?.let { navController.navigate(Screen.EditMember.createRoute(it.id)) } }) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.primary,
              titleContentColor = MaterialTheme.colorScheme.onPrimary,
              navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
              actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
          )
        },
        containerColor = Color.Transparent
      ) { scaffoldPadding ->
        if (pet != null) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(scaffoldPadding)
              .verticalScroll(scrollState)
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // Foto de cabecera con Parallax
            if (pet!!.fotoUri != null) {
              AsyncImage(
                model = pet!!.fotoUri,
                contentDescription = null,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
                  .graphicsLayer {
                      translationY = scrollState.value * 0.3f
                      alpha = 1f - (scrollState.value.toFloat() / 400f).coerceIn(0f, 1f)
                  }
                  .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
              )
            }

            // Información básica
            AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.family_general_info), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.family_type_format, pet!!.tipo))
                pet!!.raza?.let { Text(stringResource(R.string.family_breed_format, it)) }
                pet!!.numeroChip?.let { Text(stringResource(R.string.family_chip_format, it)) }
              }
            }

            // Gráfica de Peso
            if (pesos.size >= 2) {
              Text(stringResource(R.string.family_weight_evolution), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              WeightTrendGraph(pesos = pesos.sortedBy { it.fecha })
            }

            // Secciones
            SectionHeader(stringResource(R.string.dashboard_deworming_label), { showDewormingDialog = true })
            if (desparasitaciones.isEmpty()) {
              Text(stringResource(R.string.family_no_records), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp))
            } else {
              desparasitaciones.forEach { item ->
                PetListItem(item.producto ?: stringResource(R.string.dashboard_no_product), "${item.tipo} · ${formatDate(item.fechaAplicacion)}", Icons.Default.BugReport, { viewModel.deleteDesparasitacion(item) })
              }
            }

            SectionHeader(stringResource(R.string.dashboard_medications_label), { showMedicationDialog = true })
            if (medicaciones.isEmpty()) {
              Text(stringResource(R.string.family_no_meds), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp))
            } else {
              medicaciones.forEach { med ->
                PetListItem(med.nombre, "${med.dosis} · ${med.frecuencia}", Icons.Default.Medication, { viewModel.deleteMedicacion(med) }, { editingMedication = med })
              }
            }
            
            // ... (otras secciones similares)
            
            Spacer(modifier = Modifier.height(24.dp))
          }
        } else {
          Column(
            modifier = Modifier.padding(scaffoldPadding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).skeletonShimmer())
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(MaterialTheme.shapes.large).skeletonShimmer())
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(MaterialTheme.shapes.large).skeletonShimmer())
          }
        }
      }
    }
  }
}

@Composable
fun SectionHeader(title: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = onAdd) { Text(stringResource(R.string.family_btn_add)) }
    }
}

@Composable
fun PetListItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = {
            Row {
                onEdit?.let {
                    IconButton(onClick = it) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) }
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun WeightTrendGraph(pesos: List<PetWeight>) {
  val color = MaterialTheme.colorScheme.primary
  val data = pesos.map { it.pesoKg.toFloat() }
  val minWeight = (data.minOrNull() ?: 0f) * 0.95f
  val maxWeight = (data.maxOrNull() ?: 1f) * 1.05f
  val range = (maxWeight - minWeight).coerceAtLeast(1f)

  Card(
    modifier = Modifier.fillMaxWidth().height(150.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)) {
      val width = size.width
      val height = size.height
      val stepX = if (data.size > 1) width / (data.size - 1) else width
      
      val points = data.mapIndexed { index, weight ->
        val x = index * stepX
        val y = height - ((weight - minWeight) / range * height)
        Offset(x, y)
      }

      val path = Path().apply {
        points.forEachIndexed { index, offset ->
          if (index == 0) moveTo(offset.x, offset.y)
          else lineTo(offset.x, offset.y)
        }
      }

      drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
      )

      points.forEach { offset ->
        drawCircle(color = color, radius = 4.dp.toPx(), center = offset)
        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = offset)
      }
    }
  }
}

@Composable
fun MedicationActionDialog(
  item: PetMedication? = null,
  onDismiss: () -> Unit,
  onConfirm: (String, String, String) -> Unit
) {
  var nombre by remember { mutableStateOf(item?.nombre ?: "") }
  var dosis by remember { mutableStateOf(item?.dosis ?: "") }
  var frecuencia by remember { mutableStateOf(item?.frecuencia ?: "") }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
      delay(300)
      focusRequester.requestFocus()
      keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(if (item == null) R.string.dashboard_new_med_title else R.string.dashboard_edit_med_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = nombre, 
            onValueChange = { nombre = it }, 
            label = { Text(stringResource(R.string.dashboard_label_med_name)) }, 
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        OutlinedTextField(
            value = dosis, 
            onValueChange = { dosis = it }, 
            label = { Text(stringResource(R.string.dashboard_label_med_dose)) }, 
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        OutlinedTextField(
            value = frecuencia, 
            onValueChange = { frecuencia = it }, 
            label = { Text(stringResource(R.string.dashboard_label_med_freq)) }, 
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
      }
    },
    confirmButton = {
      Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre, dosis, frecuencia) }) {
        Text(stringResource(R.string.dashboard_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) }
    }
  )
}

@Composable
fun VaccineDialog(
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var nombre by remember { mutableStateOf("") }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
      delay(300)
      focusRequester.requestFocus()
      keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.dashboard_register_vaccine_title)) },
    text = {
      OutlinedTextField(
        value = nombre,
        onValueChange = { nombre = it },
        label = { Text(stringResource(R.string.dashboard_label_vaccine_name)) },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
      )
    },
    confirmButton = {
      Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre) }) {
        Text(stringResource(R.string.dashboard_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) }
    }
  )
}

@Composable
fun DewormingDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit
) {
  var producto by remember { mutableStateOf("") }
  var tipo by remember { mutableStateOf("AMBAS") }
  var expanded by remember { mutableStateOf(false) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
      delay(300)
      focusRequester.requestFocus()
      keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.dashboard_new_deworming_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = producto, 
            onValueChange = { producto = it }, 
            label = { Text(stringResource(R.string.dashboard_label_product_name)) }, 
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        
        Box {
          OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.family_type_format, tipo))
          }
          DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("INTERNA", "EXTERNA", "AMBAS").forEach { t ->
              DropdownMenuItem(text = { Text(t) }, onClick = { tipo = t; expanded = false })
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (producto.isNotBlank()) onConfirm(tipo, producto) }) {
        Text(stringResource(R.string.dashboard_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) }
    }
  )
}

@Composable
fun WeightDialog(
  onDismiss: () -> Unit,
  onConfirm: (Double) -> Unit
) {
  var weightInput by remember { mutableStateOf("") }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
      delay(300)
      focusRequester.requestFocus()
      keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.dashboard_register_weight_title)) },
    text = {
      OutlinedTextField(
        value = weightInput,
        onValueChange = { weightInput = it },
        label = { Text(stringResource(R.string.dashboard_label_weight_kg)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
      )
    },
    confirmButton = {
      Button(
        onClick = {
          weightInput.toDoubleOrNull()?.let { onConfirm(it) }
        }
      ) {
        Text(stringResource(R.string.dashboard_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.dashboard_cancel)) }
    }
  )
}

private fun formatDate(timestamp: Long): String {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
