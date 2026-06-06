package com.appcasa.core.domain.usecase.tasks

import com.appcasa.core.domain.model.Task
import com.appcasa.core.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveTasksUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Task>> {
        return repository.getTasksPaged(hogarId, limit = page * 20, offset = 0)
    }
}
