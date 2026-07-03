package com.appcasa.features.lists.domain.usecase

import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.repository.ListsRepository
import javax.inject.Inject

class UpdateListUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    suspend operator fun invoke(lista: Lista, nuevoNombre: String) {
        repository.upsertLista(lista.copy(nombre = nuevoNombre, updatedAt = System.currentTimeMillis()))
    }
}
