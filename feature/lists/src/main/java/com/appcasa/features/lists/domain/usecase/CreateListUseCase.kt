package com.appcasa.features.lists.domain.usecase

import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.TipoLista
import com.appcasa.core.domain.repository.ListsRepository
import javax.inject.Inject

class CreateListUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    suspend operator fun invoke(hogarId: String, nombre: String, tipo: String) {
        val lista = Lista(
            hogarId = hogarId,
            nombre = nombre,
            tipo = try { TipoLista.valueOf(tipo) } catch (e: Exception) { TipoLista.PERSONALIZADA }
        )
        repository.upsertLista(lista)
    }
}
