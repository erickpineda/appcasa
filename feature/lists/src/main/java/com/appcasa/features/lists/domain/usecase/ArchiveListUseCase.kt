package com.appcasa.features.lists.domain.usecase

import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.repository.ListsRepository
import javax.inject.Inject

class ArchiveListUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    suspend operator fun invoke(lista: Lista) {
        repository.upsertLista(lista.copy(archived = true))
    }
}
