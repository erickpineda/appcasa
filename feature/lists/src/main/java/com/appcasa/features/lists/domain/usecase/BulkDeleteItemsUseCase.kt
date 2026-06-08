package com.appcasa.features.lists.domain.usecase

import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.repository.ListsRepository
import javax.inject.Inject

class BulkDeleteItemsUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    suspend operator fun invoke(items: List<ListaItem>) {
        repository.deleteItems(items)
    }
}
