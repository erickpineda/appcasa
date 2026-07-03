package com.appcasa.features.lists.domain.usecase

import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.repository.ListsRepository
import javax.inject.Inject

class AddListItemUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    suspend operator fun invoke(listaId: String, texto: String) {
        repository.upsertItem(
            ListaItem(
                listaId = listaId,
                texto = texto
            )
        )
    }
}
