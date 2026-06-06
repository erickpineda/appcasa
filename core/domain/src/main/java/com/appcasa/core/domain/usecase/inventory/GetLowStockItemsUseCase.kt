package com.appcasa.core.domain.usecase.inventory

import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLowStockItemsUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke(hogarId: Long): Flow<List<StockItem>> {
        return repository.getLowStockItems(hogarId)
    }
}
