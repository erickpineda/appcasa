package com.appcasa.features.lists.domain.usecase

import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.repository.ListsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArchivedListsUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Lista>> {
        return repository.getArchivedListasPaged(hogarId, limit = page * 20, offset = 0)
    }
}
