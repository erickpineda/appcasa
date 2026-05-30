package com.appcasa.features.finance.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.finance.data.local.ExpenseEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
  private val expenseDao: ExpenseDao,
  private val configuracionDao: ConfiguracionDao,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val _ocrResult = MutableStateFlow<Double?>(null)
  val ocrResult = _ocrResult.asStateFlow()

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  fun processTicket(bitmap: Bitmap) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    recognizer.process(image)
        .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
            val text = visionText.text
            // Lógica simple: buscar el número más alto que parezca un total
            val prices = Regex("""\d+[.,]\d{2}""").findAll(text)
                .map { it.value.replace(",", ".").toDouble() }
                .toList()
            _ocrResult.value = prices.maxOrNull()
        }
  }

  fun clearOcr() { _ocrResult.value = null }

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val expenses: StateFlow<List<ExpenseEntity>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> expenseDao.getExpensesByHogar(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currencySymbol: StateFlow<String> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
      configuracionDao.getConfiguracion(id)
        .map { list -> list.find { it.clave == "moneda" }?.valor ?: "€" }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "€")

  fun addExpense(concepto: String, importe: Double, categoria: String) {
    viewModelScope.launch {
      expenseDao.insertExpense(
        ExpenseEntity(
          hogarId = householdId,
          concepto = concepto,
          importe = importe,
          categoria = categoria
        )
      )
    }
  }

  fun deleteExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      expenseDao.deleteExpense(expense)
    }
  }

  fun updateExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      expenseDao.insertExpense(expense)
    }
  }
}
