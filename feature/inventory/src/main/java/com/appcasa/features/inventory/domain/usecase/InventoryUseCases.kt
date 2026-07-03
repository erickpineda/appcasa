package com.appcasa.features.inventory.domain.usecase

import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStockUseCase @Inject constructor(
  private val repository: InventoryRepository
) {
  operator fun invoke(hogarId: String, page: Int): Flow<List<StockItem>> {
    return repository.getStockPaged(hogarId, limit = page * 20, offset = 0)
  }
}

class AddStockItemUseCase @Inject constructor(
  private val repository: InventoryRepository
) {
  suspend operator fun invoke(
    hogarId: String,
    nombre: String,
    categoria: String,
    cantidadActual: Double,
    cantidadMinima: Double,
    unidad: String,
    autoComprar: Boolean = true
  ) {
    repository.upsertStockItem(
      StockItem(
        hogarId = hogarId,
        nombre = nombre,
        categoria = categoria,
        cantidadActual = cantidadActual,
        cantidadMinima = cantidadMinima,
        unidad = unidad,
        autoComprar = autoComprar
      )
    )
  }
}

class UpdateStockItemUseCase @Inject constructor(
  private val repository: InventoryRepository
) {
  suspend operator fun invoke(item: StockItem) {
    repository.upsertStockItem(item.copy(updatedAt = System.currentTimeMillis()))
  }
}

class UpdateStockQuantityUseCase @Inject constructor(
  private val updateStockItemUseCase: UpdateStockItemUseCase,
  private val autoRestockStockItemUseCase: AutoRestockStockItemUseCase
) {
  suspend operator fun invoke(item: StockItem, delta: Double) {
    val newQuantity = (item.cantidadActual + delta).coerceAtLeast(0.0)
    val updatedItem = item.copy(cantidadActual = newQuantity)
    updateStockItemUseCase(updatedItem)
    autoRestockStockItemUseCase(updatedItem)
  }
}

class DeleteStockItemUseCase @Inject constructor(
  private val repository: InventoryRepository
) {
  suspend operator fun invoke(item: StockItem) {
    repository.deleteStockItem(item)
  }
}
