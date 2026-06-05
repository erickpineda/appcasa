package com.appcasa.features.lists.domain.usecase

import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.repository.ListsRepository
import javax.inject.Inject

class DeleteListItemUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    suspend operator fun invoke(item: ListaItem) {
        repository.deleteItem(item)
    }
}
