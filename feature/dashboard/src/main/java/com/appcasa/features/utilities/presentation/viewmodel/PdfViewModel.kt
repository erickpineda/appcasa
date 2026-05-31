package com.appcasa.features.utilities.presentation.viewmodel

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

data class PdfImageItem(
  val id: String = UUID.randomUUID().toString(),
  val uri: Uri,
  val rotation: Float = 0f,
  val isGrayscale: Boolean = false
)

@HiltViewModel
class PdfViewModel @Inject constructor(
  @ApplicationContext private val context: Context
) : ViewModel() {

  private val _selectedImages = MutableStateFlow<List<PdfImageItem>>(emptyList())
  val selectedImages = _selectedImages.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating = _isGenerating.asStateFlow()

  private val _pdfUri = MutableStateFlow<Uri?>(null)
  val pdfUri = _pdfUri.asStateFlow()

  fun addImages(uris: List<Uri>) {
    _selectedImages.value = _selectedImages.value + uris.map { PdfImageItem(uri = it) }
  }

  fun removeImage(item: PdfImageItem) {
    _selectedImages.value = _selectedImages.value.filter { it.id != item.id }
  }

  fun rotateImage(item: PdfImageItem) {
    _selectedImages.value = _selectedImages.value.map {
      if (it.id == item.id) it.copy(rotation = (it.rotation + 90f) % 360f) else it
    }
  }

  fun toggleGrayscale(item: PdfImageItem) {
    _selectedImages.value = _selectedImages.value.map {
      if (it.id == item.id) it.copy(isGrayscale = !it.isGrayscale) else it
    }
  }

  fun moveImageUp(index: Int) {
    if (index > 0) {
      val list = _selectedImages.value.toMutableList()
      Collections.swap(list, index, index - 1)
      _selectedImages.value = list
    }
  }

  fun moveImageDown(index: Int) {
    if (index < _selectedImages.value.size - 1) {
      val list = _selectedImages.value.toMutableList()
      Collections.swap(list, index, index + 1)
      _selectedImages.value = list
    }
  }

  fun generatePdf(fileName: String) {
    viewModelScope.launch {
      _isGenerating.value = true
      try {
        val file = withContext(Dispatchers.IO) {
          createPdf(fileName, _selectedImages.value)
        }
        _pdfUri.value = FileProvider.getUriForFile(context, "com.appcasa.fileprovider", file)
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        _isGenerating.value = false
      }
    }
  }

  private suspend fun createPdf(fileName: String, items: List<PdfImageItem>): File = withContext(Dispatchers.IO) {
    val pdfDocument = PdfDocument()
    
    items.forEachIndexed { index, item ->
      var bitmap = context.contentResolver.openInputStream(item.uri)?.use { 
        BitmapFactory.decodeStream(it)
      } ?: return@forEachIndexed

      // Aplicar Rotación
      if (item.rotation != 0f) {
        val matrix = Matrix().apply { postRotate(item.rotation) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        bitmap = rotated
      }

      // Aplicar Filtro Blanco y Negro
      if (item.isGrayscale) {
        val bwBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bwBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        bitmap.recycle()
        bitmap = bwBitmap
      }

      val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
      val page = pdfDocument.startPage(pageInfo)
      
      page.canvas.drawBitmap(bitmap, 0f, 0f, null)
      pdfDocument.finishPage(page)
      bitmap.recycle()
    }

    val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AppCasa")
    if (!directory.exists()) directory.mkdirs()
    
    val safeFileName = fileName.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
    val file = File(directory, "${safeFileName}.pdf")
    FileOutputStream(file).use { 
      pdfDocument.writeTo(it)
    }
    pdfDocument.close()
    file
  }
}
