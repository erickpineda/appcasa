package com.appcasa.features.inventory.domain.usecase

import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStockUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<StockItem>> {
        return repository.getStockPaged(hogarId, limit = page * 20, offset = 0)
    }
}

class AddStockItemUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(
        hogarId: Long,
        nombre: String,
        categoria: String,
        cantidadActual: Double,
        cantidadMinima: Double,
        unidad: String,
        autoComprar: Boolean = true
    ) {
        repository.insertStockItem(
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
        repository.insertStockItem(item.copy(updatedAt = System.currentTimeMillis()))
    }
}

class DeleteStockItemUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(item: StockItem) {
        repository.deleteStockItem(item)
    }
}
