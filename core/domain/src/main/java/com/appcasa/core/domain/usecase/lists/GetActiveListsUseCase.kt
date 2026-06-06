package com.appcasa.core.domain.usecase.lists

import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.repository.ListsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveListsUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Lista>> {
        return repository.getListasPaged(hogarId, limit = page * 20, offset = 0)
    }
}
