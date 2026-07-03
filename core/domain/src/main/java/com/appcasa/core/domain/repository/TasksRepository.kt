package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    // Tareas
    fun getTasksByHogar(hogarId: String): Flow<List<Task>>
    fun getTasksPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Task>>
    fun getArchivedTasksPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Task>>
    fun getTaskById(taskId: String): Flow<Task?>
    suspend fun upsertTask(task: Task): String
    suspend fun deleteTask(task: Task)
    suspend fun updateTaskStatus(taskId: String, status: EstadoTarea)
    suspend fun unarchiveTarea(id: String)
    suspend fun archiveOldCompletedTasks(hogarId: String, threshold: Long)
    suspend fun deleteAllArchivedTasks(hogarId: String)
    fun getAllCheckItemsCounts(hogarId: String): Flow<Map<String, Pair<Int, Int>>>

    // Recompensas
    fun getRewardsByHogar(hogarId: String): Flow<List<Reward>>
    suspend fun upsertReward(reward: Reward)
    suspend fun deleteReward(reward: Reward)
    suspend fun updateRewardSyncTimestamp(rewardId: String)

    // Categorías
    fun getCategoriesByHogar(hogarId: String): Flow<List<TaskCategory>>
    suspend fun upsertCategory(category: TaskCategory)
    suspend fun updateCategorySyncTimestamp(categoryId: String)

    // Asignaciones e Ítems
    fun getAssignmentsForTask(taskId: String): Flow<List<TaskAssignment>>
    suspend fun upsertAssignment(assignment: TaskAssignment)
    fun getCheckItemsForTask(taskId: String): Flow<List<TaskCheckItem>>
    suspend fun upsertCheckItem(item: TaskCheckItem): String
    suspend fun deleteCheckItem(item: TaskCheckItem)
    suspend fun updateTaskSyncTimestamp(taskId: String)
    suspend fun updateTaskHogarSyncId(taskId: String, hogarSyncId: String)
    suspend fun updateCheckItemSyncTimestamp(itemId: String)
    suspend fun getCheckItemsToSync(hogarId: String): List<TaskCheckItem>
    fun startRemoteSync(hogarId: String)
}
