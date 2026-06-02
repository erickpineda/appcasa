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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

  private val _activePage = MutableStateFlow(1)
  val activePage = _activePage.asStateFlow()

  private val _archivedPage = MutableStateFlow(1)
  val archivedPage = _archivedPage.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _toastEvent = MutableSharedFlow<String>(replay = 0)
  val toastEvent = _toastEvent.asSharedFlow()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val expenses: StateFlow<List<ExpenseEntity>> = combine(
    currentHouseholdProvider.householdId,
    _activePage
  ) { id, page -> id to page }
    .flatMapLatest { (id, page) -> 
        expenseDao.getExpensesPaged(id, limit = page * 20, offset = 0)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val archivedExpenses: StateFlow<List<ExpenseEntity>> = combine(
    currentHouseholdProvider.householdId,
    _archivedPage
  ) { id, page -> id to page }
    .flatMapLatest { (id, page) -> 
        expenseDao.getArchivedExpensesPaged(id, limit = page * 20, offset = 0)
    }
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

  fun processTicket(bitmap: Bitmap) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    recognizer.process(image)
        .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
            val text = visionText.text
            
            val prices = Regex("""\d+[.,]\d{2}""").findAll(text)
                .map { it.value.replace(",", ".").toDouble() }
                .toList()
            val total = prices.maxOrNull()
            
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

  fun archiveExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      expenseDao.insertExpense(expense.copy(archived = true))
    }
  }

  fun unarchiveExpense(expenseId: Long) {
    viewModelScope.launch {
      expenseDao.unarchiveExpense(expenseId)
    }
  }

  fun loadMoreActive() {
    if (_isLoading.value) return
    val currentCount = expenses.value.size
    _activePage.value += 1
    viewModelScope.launch {
        _isLoading.value = true
        kotlinx.coroutines.delay(600)
        if (expenses.value.size <= currentCount) {
            _toastEvent.emit("No hay más gastos para cargar")
            _activePage.value -= 1
        }
        _isLoading.value = false
    }
  }

  fun loadMoreArchived() {
    if (_isLoading.value) return
    val currentCount = archivedExpenses.value.size
    _archivedPage.value += 1
    viewModelScope.launch {
        _isLoading.value = true
        kotlinx.coroutines.delay(600)
        if (archivedExpenses.value.size <= currentCount) {
            _toastEvent.emit("No hay más registros en el archivo")
            _archivedPage.value -= 1
        }
        _isLoading.value = false
    }
  }

  fun clearAllArchived() {
    viewModelScope.launch {
      expenseDao.deleteAllArchivedExpenses(householdId)
    }
  }

  fun updateExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      expenseDao.insertExpense(expense)
    }
  }

  fun archiveOldExpenses() {
    viewModelScope.launch {
      val threshold = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000) // 1 año
      expenseDao.archiveOldExpenses(householdId, threshold)
    }
  }

  fun purgeOldPhotos() {
    viewModelScope.launch {
      val threshold = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000) // 1 año
      expenseDao.purgeOldExpensePhotos(householdId, threshold)
    }
  }
}
