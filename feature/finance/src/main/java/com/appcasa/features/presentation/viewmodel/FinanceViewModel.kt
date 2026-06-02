package com.appcasa.features.finance.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.finance.data.local.ExpenseEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
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

  private val _ocrStore = MutableStateFlow<String?>(null)
  val ocrStore = _ocrStore.asStateFlow()

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  fun processTicket(bitmap: Bitmap) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    recognizer.process(image)
        .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
            val text = visionText.text
            
            // 1. Detectar Importe (número más alto)
            val prices = Regex("""\d+[.,]\d{2}""").findAll(text)
                .map { it.value.replace(",", ".").toDouble() }
                .toList()
            val total = prices.maxOrNull()
            
            // 2. Intentar detectar Comercio (Primera línea no vacía que no sea fecha)
            val lines = text.lines().filter { it.isNotBlank() }
            val store = lines.firstOrNull { !it.contains(Regex("""\d{2}/\d{2}""")) }
            
            _ocrResult.value = total
            _ocrStore.value = store
        }
  }

  fun clearOcr() { 
      _ocrResult.value = null 
      _ocrStore.value = null
  }

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val expenses: StateFlow<List<ExpenseEntity>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> expenseDao.getExpensesByHogar(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val expensesByCategory: StateFlow<Map<String, Double>> = expenses
    .map { list -> 
        list.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.importe } }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val monthlyEvolution: StateFlow<Map<String, Double>> = expenses
    .map { list ->
        val sdf = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
        list.groupBy { sdf.format(java.util.Date(it.fecha)) }
            .mapValues { entry -> entry.value.sumOf { it.importe } }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currencySymbol: StateFlow<String> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
      configuracionDao.getConfiguracion(id)
        .map { list -> list.find { it.clave == "moneda" }?.valor ?: "€" }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "€")

  fun addExpense(concepto: String, importe: Double, categoria: String, fotoUri: String? = null) {
    viewModelScope.launch {
      expenseDao.insertExpense(
        ExpenseEntity(
          hogarId = householdId,
          concepto = concepto,
          importe = importe,
          categoria = categoria,
          fotoUri = fotoUri
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
