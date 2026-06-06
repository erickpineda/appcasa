package com.appcasa.features.finance.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.Expense
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.*
import com.appcasa.features.finance.domain.usecase.*
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
  private val getExpensesUseCase: GetExpensesUseCase,
  private val getArchivedExpensesUseCase: GetArchivedExpensesUseCase,
  private val getTotalMonthlyExpenseUseCase: GetTotalMonthlyExpenseUseCase,
  private val addExpenseUseCase: AddExpenseUseCase,
  private val deleteExpenseUseCase: DeleteExpenseUseCase,
  private val unarchiveExpenseUseCase: UnarchiveExpenseUseCase,
  private val clearAllArchivedExpensesUseCase: ClearAllArchivedExpensesUseCase,
  private val archiveOldExpensesUseCase: ArchiveOldExpensesUseCase,
  private val purgeOldPhotosUseCase: PurgeOldPhotosUseCase,
  private val updateExpenseUseCase: UpdateExpenseUseCase,
  private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase,
  private val processTicketUseCase: ProcessTicketUseCase,
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
  val expenses: StateFlow<List<Expense>> = combine(
    currentHouseholdProvider.householdId,
    _activePage
  ) { id, page -> id to page }
    .flatMapLatest { (id, page) -> 
        getExpensesUseCase(id, page)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val archivedExpenses: StateFlow<List<Expense>> = combine(
    currentHouseholdProvider.householdId,
    _archivedPage
  ) { id, page -> id to page }
    .flatMapLatest { (id, page) -> 
        getArchivedExpensesUseCase(id, page)
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
    .flatMapLatest { id -> getCurrencySymbolUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "€")

  fun processTicket(bitmap: Bitmap) {
    viewModelScope.launch {
      val result = processTicketUseCase(bitmap)
      _ocrResult.value = result.total
      _ocrStore.value = result.store
    }
  }

  fun clearOcr() { 
      _ocrResult.value = null 
      _ocrStore.value = null
  }

  fun addExpense(concepto: String, importe: Double, categoria: String, fotoUri: String? = null) {
    viewModelScope.launch {
      addExpenseUseCase(householdId, concepto, importe, categoria, fotoUri)
    }
  }

  fun deleteExpense(expense: Expense) {
    viewModelScope.launch {
      deleteExpenseUseCase(expense)
    }
  }

  fun archiveExpense(expense: Expense) {
    viewModelScope.launch {
      updateExpenseUseCase(expense.copy(archived = true))
    }
  }

  fun unarchiveExpense(expenseId: Long) {
    viewModelScope.launch {
      unarchiveExpenseUseCase(expenseId)
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
      clearAllArchivedExpensesUseCase(householdId)
    }
  }

  fun updateExpense(expense: Expense) {
    viewModelScope.launch {
      updateExpenseUseCase(expense)
    }
  }

  fun archiveOldExpenses() {
    viewModelScope.launch {
      archiveOldExpensesUseCase(householdId)
    }
  }

  fun purgeOldPhotos() {
    viewModelScope.launch {
      purgeOldPhotosUseCase(householdId)
    }
  }
}
