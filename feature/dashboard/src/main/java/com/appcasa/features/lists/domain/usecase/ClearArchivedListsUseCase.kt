package com.appcasa.features.lists.domain.usecase

import com.appcasa.core.domain.repository.ListsRepository
import javax.inject.Inject

class ClearArchivedListsUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        repository.deleteAllArchivedListas(hogarId)
    }
}
