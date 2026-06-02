package com.appcasa.features.inventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.inventory.data.local.StockDao
import com.appcasa.features.inventory.data.local.StockEntity
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.lists.data.local.ListaItemEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
  private val stockDao: StockDao,
  private val listaDao: ListaDao,
  private val configuracionDao: ConfiguracionDao,
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
  val stockItems: StateFlow<List<StockEntity>> = combine(
    currentHouseholdProvider.householdId,
    _activePage
  ) { id, page -> id to page }
    .flatMapLatest { (id, page) -> 
        stockDao.getStockPaged(id, limit = page * 20, offset = 0)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val availableLists: StateFlow<List<ListaEntity>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> listaDao.getListasPaged(id, limit = 50, offset = 0) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val isCompactView: StateFlow<Boolean> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
      configuracionDao.getConfiguracion(id)
        .map { list -> list.find { it.clave == "vista_compacta" }?.valor == "true" }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  fun addItem(nombre: String, categoria: String, actual: Double, minima: Double, unidad: String) {
    viewModelScope.launch {
      stockDao.insertItem(
        StockEntity(
          hogarId = householdId,
          nombre = nombre,
          categoria = categoria,
          cantidadActual = actual,
          cantidadMinima = minima,
          unidad = unidad
        )
      )
    }
  }

  fun updateQuantity(item: StockEntity, delta: Double) {
    viewModelScope.launch {
      val newQuantity = (item.cantidadActual + delta).coerceAtLeast(0.0)
      val updatedItem = item.copy(cantidadActual = newQuantity, updatedAt = System.currentTimeMillis())
      stockDao.updateItem(updatedItem)
      
      // Lógica de reabastecimiento automático
      if (updatedItem.autoComprar && updatedItem.cantidadActual <= updatedItem.cantidadMinima) {
        val missing = (updatedItem.cantidadMinima - updatedItem.cantidadActual).coerceAtLeast(1.0)
        autoAddToPreferredList(updatedItem, missing)
      }
    }
  }

  private suspend fun autoAddToPreferredList(item: StockEntity, delta: Double) {
    val configs = configuracionDao.getConfiguracion(householdId).first()
    val preferredListId = configs.find { it.clave == "lista_compra_id" }?.valor?.toLongOrNull()
    val listId = preferredListId ?: run {
      val listList = listaDao.getListasPaged(householdId, 50, 0).first()
      listList.find { it.tipo == com.appcasa.core.domain.model.TipoLista.COMPRA.name }?.id
    }
    if (listId != null) {
      performAddToList(item, listId, delta)
    }
  }

  fun addToShoppingList(item: StockEntity, listId: Long, quantity: Double) {
    viewModelScope.launch {
      performAddToList(item, listId, quantity)
    }
  }

  private suspend fun performAddToList(item: StockEntity, listId: Long, delta: Double) {
    try {
      val itemsInList = listaDao.getItemsByLista(listId).first()
      val targetText = "COMPRAR: ${item.nombre}"
      val existingItem = itemsInList.find { it.texto == targetText && !it.completado }

      if (existingItem != null) {
        val currentQty = existingItem.cantidad?.split(" ")?.get(0)?.toDoubleOrNull() ?: 0.0
        val totalQty = currentQty + delta
        val newQtyStr = "${if (totalQty % 1 == 0.0) totalQty.toInt() else totalQty} ${item.unidad}"
        listaDao.updateItem(existingItem.copy(cantidad = newQtyStr))
      } else {
        val qtyStr = "${if (delta % 1 == 0.0) delta.toInt() else delta} ${item.unidad}"
        listaDao.insertItem(ListaItemEntity(listaId = listId, texto = targetText, cantidad = qtyStr))
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun updateItem(item: StockEntity) {
    viewModelScope.launch {
      stockDao.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
    }
  }

  fun deleteItem(item: StockEntity) {
    viewModelScope.launch {
      stockDao.deleteItem(item)
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
