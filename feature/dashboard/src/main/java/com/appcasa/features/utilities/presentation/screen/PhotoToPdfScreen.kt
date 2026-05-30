package com.appcasa.features.utilities.presentation.screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.features.utilities.presentation.viewmodel.PdfViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoToPdfScreen(
  navController: NavController,
  viewModel: PdfViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val selectedImages by viewModel.selectedImages.collectAsState()
  val isGenerating by viewModel.isGenerating.collectAsState()
  val pdfUri by viewModel.pdfUri.collectAsState()
  
  var fileName by remember { mutableStateOf("Mi_Documento") }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris ->
    viewModel.addImages(uris)
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = { Text("Fotos a PDF") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
          }
        },
        actions = {
          if (selectedImages.isNotEmpty()) {
            IconButton(onClick = { viewModel.generatePdf(fileName) }) {
              Icon(Icons.Default.PictureAsPdf, contentDescription = "Generar PDF")
            }
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      OutlinedTextField(
        value = fileName,
        onValueChange = { fileName = it },
        label = { Text("Nombre del archivo") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = { Text(".pdf", modifier = Modifier.padding(end = 8.dp)) }
      )

      if (selectedImages.isEmpty()) {
        Box(
          modifier = Modifier
            .height(200.dp)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Button(onClick = { launcher.launch("image/*") }) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Seleccionar Fotos")
          }
        }
      } else {
        Box(modifier = Modifier.height(300.dp)) {
          LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(selectedImages) { uri ->
              Box {
                AppCasaCard(
                  useGlassmorphism = true,
                  modifier = Modifier.aspectRatio(1f)
                ) {
                  AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                }
                IconButton(
                  onClick = { viewModel.removeImage(uri) },
                  modifier = Modifier.align(Alignment.TopEnd)
                ) {
                  Icon(Icons.Default.Cancel, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
              }
            }
            item {
              Box(
                modifier = Modifier
                  .aspectRatio(1f)
                  .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Añadir más", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
      }

      if (isGenerating) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      }

      if (pdfUri != null) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = {
              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
              }
              context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
            },
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Compartir")
          }

          val safeViewModel: com.appcasa.features.utilities.presentation.viewmodel.SmartSafeViewModel = hiltViewModel()
          Button(
            onClick = {
              safeViewModel.addDocumento(fileName, "Otros", pdfUri.toString())
              scope.launch {
                snackbarHostState.showSnackbar("Guardado en Smart Safe")
              }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
          ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Guardar Safe")
          }
        }
      }
    }
  }
}
