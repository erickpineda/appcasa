package com.appcasa.features.utilities.presentation.screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris ->
    viewModel.addImages(uris)
  }

  Scaffold(
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
        // En landscape, una Grid con altura fija permite ver las fotos y seguir bajando por el resto del formulario
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
        Button(
          onClick = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
              type = "application/pdf"
              putExtra(Intent.EXTRA_STREAM, pdfUri)
              addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Share, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text("Compartir PDF Generado")
        }
      }
    }
  }
}
