package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    // Tareas
    fun getTasksByHogar(hogarId: Long): Flow<List<Task>>
    fun getTasksPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Task>>
    fun getArchivedTasksPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Task>>
    fun getTaskById(taskId: Long): Flow<Task?>
    suspend fun insertTask(task: Task): Long
    suspend fun deleteTask(task: Task)
    suspend fun updateTaskStatus(taskId: Long, status: EstadoTarea)
    suspend fun unarchiveTarea(id: Long)
    suspend fun archiveOldCompletedTasks(hogarId: Long, threshold: Long)
    suspend fun deleteAllArchivedTasks(hogarId: Long)
    fun getAllCheckItemsCounts(hogarId: Long): Flow<Map<Long, Pair<Int, Int>>>

    // Recompensas
    fun getRewardsByHogar(hogarId: Long): Flow<List<Reward>>
    suspend fun insertReward(reward: Reward)
    suspend fun deleteReward(reward: Reward)

    // Categorías
    fun getCategoriesByHogar(hogarId: Long): Flow<List<TaskCategory>>
    suspend fun insertCategory(category: TaskCategory)

    // Asignaciones e Ítems
    fun getAssignmentsForTask(taskId: Long): Flow<List<TaskAssignment>>
    suspend fun insertAssignment(assignment: TaskAssignment)
    fun getCheckItemsForTask(taskId: Long): Flow<List<TaskCheckItem>>
    suspend fun insertCheckItem(item: TaskCheckItem): Long
    suspend fun updateCheckItem(item: TaskCheckItem)
    suspend fun deleteCheckItem(item: TaskCheckItem)

    suspend fun updateTaskSyncTimestamp(taskId: Long)

    fun startRemoteSync(hogarId: Long)
}
