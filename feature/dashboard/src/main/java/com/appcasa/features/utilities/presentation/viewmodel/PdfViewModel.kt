package com.appcasa.features.utilities.presentation.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
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
import javax.inject.Inject

@HiltViewModel
class PdfViewModel @Inject constructor(
  @ApplicationContext private val context: Context
) : ViewModel() {

  private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
  val selectedImages = _selectedImages.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating = _isGenerating.asStateFlow()

  private val _pdfUri = MutableStateFlow<Uri?>(null)
  val pdfUri = _pdfUri.asStateFlow()

  fun addImages(uris: List<Uri>) {
    _selectedImages.value = _selectedImages.value + uris
  }

  fun removeImage(uri: Uri) {
    _selectedImages.value = _selectedImages.value - uri
  }

  fun generatePdf(fileName: String) {
    viewModelScope.launch {
      _isGenerating.value = true
      try {
        val file = withContext(Dispatchers.IO) {
          createPdf(fileName, _selectedImages.value)
        }
        // Usamos FileProvider para evitar FileUriExposedException en Android 7+
        _pdfUri.value = FileProvider.getUriForFile(context, "com.appcasa.fileprovider", file)
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        _isGenerating.value = false
      }
    }
  }

  private suspend fun createPdf(fileName: String, uris: List<Uri>): File = withContext(Dispatchers.IO) {
    val pdfDocument = PdfDocument()
    
    uris.forEachIndexed { index, uri ->
      val bitmap = context.contentResolver.openInputStream(uri)?.use { 
        BitmapFactory.decodeStream(it)
      } ?: return@forEachIndexed

      val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
      val page = pdfDocument.startPage(pageInfo)
      
      page.canvas.drawBitmap(bitmap, 0f, 0f, null)
      pdfDocument.finishPage(page)
      bitmap.recycle()
    }

    val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AppCasa")
    if (!directory.exists()) directory.mkdirs()
    
    val file = File(directory, "${fileName}.pdf")
    FileOutputStream(file).use { 
      pdfDocument.writeTo(it)
    }
    pdfDocument.close()
    file
  }
}
