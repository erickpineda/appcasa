package com.appcasa.features.lists.domain.usecase

import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.repository.ListsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListItemsUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    operator fun invoke(listaId: String): Flow<List<ListaItem>> {
        return repository.getItemsByLista(listaId)
    }
}
