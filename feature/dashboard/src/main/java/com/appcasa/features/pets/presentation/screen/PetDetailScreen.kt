package com.appcasa.features.pets.presentation.screen

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.core.ui.components.skeletonShimmer
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.pets.data.local.*
import com.appcasa.features.pets.presentation.viewmodel.PetDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

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
  
  var editingMedication by remember { mutableStateOf<MascotaMedicacionEntity?>(null) }

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
            title = { Text(pet?.nombre ?: "Mascota") },
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
              }
            },
            actions = {
              IconButton(onClick = { pet?.let { navController.navigate(com.appcasa.navigation.Screen.EditMember.createRoute(it.id)) } }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
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
            com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true, modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text("Información General", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tipo: ${pet!!.tipo}")
                pet!!.raza?.let { Text("Raza: $it") }
                pet!!.numeroChip?.let { Text("Chip: $it") }
              }
            }

            // Gráfica de Peso
            if (pesos.size >= 2) {
              Text("Evolución de Peso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              WeightTrendGraph(pesos = pesos.sortedBy { it.fecha })
            }

            // Secciones
            SectionHeader("Desparasitación", { showDewormingDialog = true })
            if (desparasitaciones.isEmpty()) {
              Text("Sin registros", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp))
            } else {
              desparasitaciones.forEach { item ->
                PetListItem(item.producto ?: "Sin producto", "${item.tipo} · ${formatDate(item.fechaAplicacion)}", Icons.Default.BugReport, { viewModel.deleteDesparasitacion(item) })
              }
            }

            SectionHeader("Medicaciones Activas", { showMedicationDialog = true })
            if (medicaciones.isEmpty()) {
              Text("Sin medicaciones", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp))
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
        TextButton(onClick = onAdd) { Text("Añadir") }
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
fun WeightTrendGraph(pesos: List<MascotaPesoEntity>) {
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
  item: MascotaMedicacionEntity? = null,
  onDismiss: () -> Unit,
  onConfirm: (String, String, String) -> Unit
) {
  var nombre by remember { mutableStateOf(item?.nombre ?: "") }
  var dosis by remember { mutableStateOf(item?.dosis ?: "") }
  var frecuencia by remember { mutableStateOf(item?.frecuencia ?: "") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (item == null) "Nueva Medicación" else "Editar Medicación") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre (ej: Apoquel)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dosis, onValueChange = { dosis = it }, label = { Text("Dosis (ej: 1 pastilla)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = frecuencia, onValueChange = { frecuencia = it }, label = { Text("Frecuencia (ej: Cada 12h)") }, modifier = Modifier.fillMaxWidth())
      }
    },
    confirmButton = {
      Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre, dosis, frecuencia) }) {
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    }
  )
}

@Composable
fun VaccineDialog(
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var nombre by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Registrar Vacuna") },
    text = {
      OutlinedTextField(
        value = nombre,
        onValueChange = { nombre = it },
        label = { Text("Nombre de la vacuna") },
        modifier = Modifier.fillMaxWidth()
      )
    },
    confirmButton = {
      Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre) }) {
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
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

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Nueva Desparasitación") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = producto, onValueChange = { producto = it }, label = { Text("Producto (ej: Advocate)") }, modifier = Modifier.fillMaxWidth())
        
        Box {
          OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Tipo: $tipo")
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
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    }
  )
}

@Composable
fun WeightDialog(
  onDismiss: () -> Unit,
  onConfirm: (Double) -> Unit
) {
  var weightInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Registrar Peso") },
    text = {
      OutlinedTextField(
        value = weightInput,
        onValueChange = { weightInput = it },
        label = { Text("Peso en kg") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
    },
    confirmButton = {
      Button(
        onClick = {
          weightInput.toDoubleOrNull()?.let { onConfirm(it) }
        }
      ) {
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
