package com.appcasa.features.inventory.domain.usecase

import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.model.StockItem
import com.appcasa.core.domain.repository.ListsRepository
import com.appcasa.core.utils.Constants
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddToShoppingListUseCase @Inject constructor(
  private val listsRepository: ListsRepository
) {
  suspend operator fun invoke(item: StockItem, listId: String, delta: Double) {
    try {
      val itemsInList = listsRepository.getItemsByLista(listId).first()
      val targetText = "${Constants.Lists.PREFIX_SHOPPING_ITEM}${item.nombre}"
      val existingItem = itemsInList.find { it.texto == targetText && !it.completado }

      if (existingItem != null) {
        val currentQty = existingItem.cantidad?.split(" ")?.get(0)?.toDoubleOrNull() ?: 0.0
        val totalQty = currentQty + delta
        val newQtyStr = "${if (totalQty % 1 == 0.0) totalQty.hashCode() else totalQty} ${item.unidad}"
        listsRepository.upsertItem(existingItem.copy(cantidad = newQtyStr))
      } else {
        val qtyStr = "${if (delta % 1 == 0.0) delta.hashCode() else delta} ${item.unidad}"
        listsRepository.upsertItem(ListaItem(listaId = listId, texto = targetText, cantidad = qtyStr))
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
