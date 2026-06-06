package com.appcasa.features.inventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.lists.GetActiveListsUseCase
import com.appcasa.core.domain.usecase.config.IsCompactViewUseCase
import com.appcasa.features.inventory.domain.usecase.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
  private val getStockUseCase: GetStockUseCase,
  private val addStockItemUseCase: AddStockItemUseCase,
  private val updateStockItemUseCase: UpdateStockItemUseCase,
  private val deleteStockItemUseCase: DeleteStockItemUseCase,
  private val updateStockQuantityUseCase: UpdateStockQuantityUseCase,
  private val addToShoppingListUseCase: AddToShoppingListUseCase,
  private val getActiveListsUseCase: GetActiveListsUseCase,
  private val isCompactViewUseCase: IsCompactViewUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val _barcodeResult = MutableStateFlow<String?>(null)
  val barcodeResult = _barcodeResult.asStateFlow()

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  private val _activePage = MutableStateFlow(1)
  val activePage = _activePage.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _toastEvent = MutableSharedFlow<String>(replay = 0)
  val toastEvent = _toastEvent.asSharedFlow()

  fun scanBarcode(image: InputImage) {
    val scanner = BarcodeScanning.getClient()
    scanner.process(image)
        .addOnSuccessListener { barcodes: List<com.google.mlkit.vision.barcode.common.Barcode> ->
            for (barcode in barcodes) {
                _barcodeResult.value = barcode.rawValue
                break
            }
        }
  }

  fun clearBarcode() { _barcodeResult.value = null }

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val stockItems: StateFlow<List<StockItem>> = combine(
    currentHouseholdProvider.householdId,
    _activePage
  ) { id, page -> id to page }
    .flatMapLatest { (id, page) -> 
        getStockUseCase(id, page)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val availableLists: StateFlow<List<Lista>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getActiveListsUseCase(id, 1) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val isCompactView: StateFlow<Boolean> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> isCompactViewUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  fun addItem(nombre: String, categoria: String, actual: Double, minima: Double, unidad: String) {
    viewModelScope.launch {
      addStockItemUseCase(householdId, nombre, categoria, actual, minima, unidad)
    }
  }

  fun updateQuantity(item: StockItem, delta: Double) {
    viewModelScope.launch {
      updateStockQuantityUseCase(item, delta)
    }
  }

  fun addToShoppingList(item: StockItem, listId: Long, quantity: Double) {
    viewModelScope.launch {
      addToShoppingListUseCase(item, listId, quantity)
    }
  }

  fun updateItem(item: StockItem) {
    viewModelScope.launch {
      updateStockItemUseCase(item)
    }
  }

  fun deleteItem(item: StockItem) {
    viewModelScope.launch {
      deleteStockItemUseCase(item)
    }
  }

  fun loadMore() {
    if (_isLoading.value) return
    val currentCount = stockItems.value.size
    _activePage.value += 1
    viewModelScope.launch {
        _isLoading.value = true
        kotlinx.coroutines.delay(600)
        if (stockItems.value.size <= currentCount) {
            _toastEvent.emit("No hay más inventario para cargar")
            _activePage.value -= 1
        }
        _isLoading.value = false
    }
  }
}
