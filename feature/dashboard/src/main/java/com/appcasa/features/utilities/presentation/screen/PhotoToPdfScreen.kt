package com.appcasa.features.utilities.presentation.screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.appcasa.feature.dashboard.R
import coil3.compose.AsyncImage
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.features.utilities.presentation.viewmodel.PdfImageItem
import com.appcasa.features.utilities.presentation.viewmodel.PdfViewModel
import com.appcasa.features.utilities.presentation.viewmodel.SmartSafeViewModel
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
  
  var fileName by remember { mutableStateOf("Doc_${System.currentTimeMillis() / 100000}") }
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
        title = { Text(stringResource(R.string.util_photo_pdf_title)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
          }
        },
        actions = {
          if (selectedImages.isNotEmpty()) {
            Button(
                onClick = { viewModel.generatePdf(fileName) },
                enabled = !isGenerating,
                modifier = Modifier.padding(end = 8.dp)
            ) {
              if (isGenerating) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
              } else {
                  Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(Modifier.width(4.dp))
                  Text(stringResource(R.string.util_photo_pdf_generate))
              }
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
    ) {
      // Configuración de archivo
      AppCasaCard(
          useGlassmorphism = true,
          modifier = Modifier.padding(16.dp)
      ) {
          Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
              OutlinedTextField(
                  value = fileName,
                  onValueChange = { fileName = it },
                  label = { Text(stringResource(R.string.util_photo_pdf_filename)) },
                  modifier = Modifier.weight(1f),
                  singleLine = true,
                  trailingIcon = { Text(".pdf", modifier = Modifier.padding(end = 8.dp), style = MaterialTheme.typography.bodySmall) }
              )
              
              IconButton(
                  onClick = { launcher.launch("image/*") },
                  colors = IconButtonDefaults.filledIconButtonColors()
              ) {
                  Icon(Icons.Default.AddPhotoAlternate, contentDescription = stringResource(R.string.util_photo_pdf_cd_add_photos))
              }
          }
      }

      if (selectedImages.isEmpty()) {
          Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                  Spacer(Modifier.height(16.dp))
                  Text(stringResource(R.string.util_photo_pdf_no_photos), color = MaterialTheme.colorScheme.outline)
                  TextButton(onClick = { launcher.launch("image/*") }) {
                      Text(stringResource(R.string.util_photo_pdf_add_photos_hint))
                  }
              }
          }
      } else {
          LazyColumn(
              modifier = Modifier.weight(1f).fillMaxWidth(),
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
              itemsIndexed(selectedImages) { index, item ->
                  PdfImageRow(
                      item = item,
                      index = index,
                      isFirst = index == 0,
                      isLast = index == selectedImages.size - 1,
                      onRotate = { viewModel.rotateImage(item) },
                      onToggleBW = { viewModel.toggleGrayscale(item) },
                      onMoveUp = { viewModel.moveImageUp(index) },
                      onMoveDown = { viewModel.moveImageDown(index) },
                      onDelete = { viewModel.removeImage(item) }
                  )
              }
          }
      }

      AnimatedVisibility(visible = pdfUri != null) {
          val shareChooserTitle = stringResource(R.string.util_photo_pdf_share_chooser)
          val savedSafeMsg = stringResource(R.string.util_photo_pdf_saved_safe)
          Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
              Button(
                  onClick = {
                      val shareIntent = Intent(Intent.ACTION_SEND).apply {
                          type = "application/pdf"
                          putExtra(Intent.EXTRA_STREAM, pdfUri)
                          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                      }
                      context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
                  },
                  modifier = Modifier.weight(1f)
              ) {
                  Icon(Icons.Default.Share, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text(stringResource(R.string.util_photo_pdf_btn_share))
              }

              val safeViewModel: SmartSafeViewModel = hiltViewModel()
              Button(
                  onClick = {
                      safeViewModel.addDocumento(fileName, "Otros", pdfUri.toString())
                      scope.launch {
                          snackbarHostState.showSnackbar(savedSafeMsg)
                      }
                  },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
              ) {
                  Icon(Icons.Default.Lock, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text(stringResource(R.string.util_photo_pdf_btn_save_safe))
              }
          }
      }
    }
  }
}

@Composable
fun PdfImageRow(
    item: PdfImageItem,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onRotate: () -> Unit,
    onToggleBW: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    AppCasaCard(useGlassmorphism = true) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth().height(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Miniatura con indicador de página
            Box(modifier = Modifier.size(80.dp).clip(MaterialTheme.shapes.medium)) {
                AsyncImage(
                    model = item.uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { 
                            rotationZ = item.rotation
                        },
                    contentScale = ContentScale.Crop,
                    colorFilter = if (item.isGrayscale) androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                        androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) }
                    ) else null
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp).align(Alignment.BottomStart)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }

            // Controles
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRotate, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.RotateRight, contentDescription = stringResource(R.string.util_photo_pdf_cd_rotate), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onToggleBW, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (item.isGrayscale) Icons.Default.FilterBAndW else Icons.Default.ColorLens,
                            contentDescription = stringResource(R.string.util_photo_pdf_cd_bw_filter),
                            tint = if (item.isGrayscale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onMoveUp, enabled = !isFirst, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.util_photo_pdf_cd_move_up), tint = if (isFirst) Color.Gray else MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onMoveDown, enabled = !isLast, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.util_photo_pdf_cd_move_down), tint = if (isLast) Color.Gray else MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
    }
}
