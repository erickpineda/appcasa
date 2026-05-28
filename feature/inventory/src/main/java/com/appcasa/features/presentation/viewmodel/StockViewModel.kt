package com.appcasa.features.inventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.inventory.data.local.StockDao
import com.appcasa.features.inventory.data.local.StockEntity
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.lists.data.local.ListaItemEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
  private val stockDao: StockDao,
  private val listaDao: ListaDao,
  private val configuracionDao: ConfiguracionDao
) : ViewModel() {

  val stockItems: StateFlow<List<StockEntity>> = stockDao.getAllStock()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val availableLists: StateFlow<List<ListaEntity>> = listaDao.getListasByHogar(1L)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val isCompactView: StateFlow<Boolean> = configuracionDao.getConfiguracion(1L)
    .map { list -> list.find { it.clave == "vista_compacta" }?.valor == "true" }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  fun addItem(nombre: String, categoria: String, actual: Double, minima: Double, unidad: String) {
    viewModelScope.launch {
      stockDao.insertItem(
        StockEntity(
          hogarId = 1L,
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
      
      // Lógica de reabastecimiento automático (usa la lista por defecto)
      if (updatedItem.autoComprar && updatedItem.cantidadActual <= updatedItem.cantidadMinima) {
        val missing = (updatedItem.cantidadMinima - updatedItem.cantidadActual).coerceAtLeast(1.0)
        autoAddToPreferredList(updatedItem, missing)
      }
    }
  }

  private suspend fun autoAddToPreferredList(item: StockEntity, delta: Double) {
    val configs = configuracionDao.getConfiguracion(1L).first()
    val preferredListId = configs.find { it.clave == "lista_compra_id" }?.valor?.toLongOrNull()
    val listId = preferredListId ?: run {
      val listList = listaDao.getListasByHogar(1L).first()
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
        // Extraer cantidad numérica actual y sumar
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
}
