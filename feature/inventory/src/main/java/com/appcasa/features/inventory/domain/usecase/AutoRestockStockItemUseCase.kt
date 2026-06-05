package com.appcasa.features.inventory.domain.usecase

import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.model.TipoLista
import com.appcasa.core.domain.repository.ConfigurationRepository
import com.appcasa.core.domain.repository.ListsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AutoRestockStockItemUseCase @Inject constructor(
    private val listsRepository: ListsRepository,
    private val configurationRepository: ConfigurationRepository,
    private val addToShoppingListUseCase: AddToShoppingListUseCase
) {
    suspend operator fun invoke(item: StockItem) {
        if (!item.autoComprar || item.cantidadActual > item.cantidadMinima) return

        val missing = (item.cantidadMinima - item.cantidadActual).coerceAtLeast(1.0)
        
        val preferredListId = configurationRepository.getConfiguracion(item.hogarId).first()
            .find { it.clave == "lista_compra_id" }?.valor?.toLongOrNull()
            
        val listId = preferredListId ?: run {
          val listList = listsRepository.getListasPaged(item.hogarId, 50, 0).first()
          listList.find { it.tipo == TipoLista.COMPRA }?.id
        }
        
        if (listId != null) {
          addToShoppingListUseCase(item, listId, missing)
        }
    }
}
