package com.appcasa.features.tasks.data.repository

import com.appcasa.core.data.R
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.TaskRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.TasksRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.core.utils.NotificationHelper
import com.appcasa.features.tasks.data.local.RecompensaDao
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.mapper.toDomain
import com.appcasa.features.tasks.data.mapper.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class TasksRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val tareaDao: TareaDao,
    private val recompensaDao: RecompensaDao,
    private val familyRepository: FamilyRepository,
    private val householdRepository: HouseholdRepository,
    private val syncScheduler: SyncScheduler,
    private val syncManager: SyncManager,
    private val remoteDataSource: TaskRemoteDataSource
) : TasksRepository {

    override fun getTasksByHogar(hogarId: String): Flow<List<Task>> {
        return tareaDao.getTareasByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTasksPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Task>> {
        return tareaDao.getTareasPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedTasksPaged(hogarId: String, limit: Int, offset: Int): Flow<List<Task>> {
        return tareaDao.getArchivedTareasPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTaskById(taskId: String): Flow<Task?> {
        return flow {
            emit(tareaDao.getTareaById(taskId)?.toDomain())
        }
    }

    override suspend fun upsertTask(task: Task): String {
        var taskToInsert = task
        if (taskToInsert.id.isBlank()) {
            taskToInsert = taskToInsert.copy(id = UUID.randomUUID().toString())
        }

        tareaDao.upsertTarea(taskToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(taskToInsert.hogarId)
        return taskToInsert.id
    }

    override suspend fun deleteTask(task: Task) {
        tareaDao.deleteTarea(task.toEntity())
        syncScheduler.scheduleSync(task.hogarId)
    }

    override suspend fun updateTaskStatus(taskId: String, status: EstadoTarea) {
        val taskEntity = tareaDao.getTareaById(taskId)
        taskEntity?.let {
            val oldStatus = it.estado
            tareaDao.upsertTarea(it.copy(estado = status.name, updatedAt = System.currentTimeMillis()))
            
            if (status == EstadoTarea.COMPLETADA && oldStatus != EstadoTarea.COMPLETADA.name && !it.puntosOtorgados) {
                val assignments = tareaDao.getAsignacionesByTarea(taskId)
                assignments.forEach { assignment ->
                    familyRepository.addPointsToMember(assignment.miembroId, it.points)
                }
                tareaDao.upsertTarea(it.copy(estado = status.name, puntosOtorgados = true, updatedAt = System.currentTimeMillis()))
            }

            syncScheduler.scheduleSync(it.hogarId)
        }
    }

    override suspend fun unarchiveTarea(id: String) {
        tareaDao.unarchiveTarea(id)
    }

    override suspend fun archiveOldCompletedTasks(hogarId: String, threshold: Long) {
        tareaDao.archiveOldCompletedTasks(hogarId, threshold)
    }

    override suspend fun deleteAllArchivedTasks(hogarId: String) {
        tareaDao.softDeleteAllArchivedTasks(hogarId, System.currentTimeMillis(), "system")
    }

    override fun getAllCheckItemsCounts(hogarId: String): Flow<Map<String, Pair<Int, Int>>> {
        return tareaDao.getAllCheckItemsCounts(hogarId).map { list ->
            list.associate { it.taskId to (it.total to it.completed) }
        }
    }

    override fun getRewardsByHogar(hogarId: String): Flow<List<Reward>> {
        return recompensaDao.getRecompensasByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertReward(reward: Reward) {
        var rewardToInsert = reward
        if (rewardToInsert.id.isBlank()) {
            rewardToInsert = rewardToInsert.copy(id = UUID.randomUUID().toString())
        }
        recompensaDao.upsertRecompensa(rewardToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(rewardToInsert.hogarId)
    }

    override suspend fun deleteReward(reward: Reward) {
        recompensaDao.deleteRecompensa(reward.toEntity())
        syncScheduler.scheduleSync(reward.hogarId)
    }

    override fun getCategoriesByHogar(hogarId: String): Flow<List<TaskCategory>> {
        return tareaDao.getCategorias(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertCategory(category: TaskCategory) {
        var categoryToInsert = category
        if (categoryToInsert.id.isBlank()) {
            categoryToInsert = categoryToInsert.copy(id = UUID.randomUUID().toString())
        }
        tareaDao.upsertCategoria(categoryToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(categoryToInsert.hogarId)
    }

    override fun getAssignmentsForTask(taskId: String): Flow<List<TaskAssignment>> {
        return flow {
             val assignment = tareaDao.getAsignacionByTarea(taskId)
             emit(assignment?.let { listOf(it.toDomain()) } ?: emptyList())
        }
    }

    override suspend fun upsertAssignment(assignment: TaskAssignment) {
        val task = tareaDao.getTareaById(assignment.tareaId)
        val member = familyRepository.getMemberById(assignment.miembroId)
        
        val assignmentToInsert = assignment.copy(
            tareaId = task?.id ?: "",
            miembroId = member?.id ?: ""
        )
        tareaDao.upsertAsignacion(assignmentToInsert.toEntity())
    }

    override fun getCheckItemsForTask(taskId: String): Flow<List<TaskCheckItem>> {
        return tareaDao.getCheckItems(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertCheckItem(item: TaskCheckItem): String {
        var itemToInsert = item
        if (itemToInsert.id.isBlank()) {
            itemToInsert = itemToInsert.copy(id = UUID.randomUUID().toString())
        }
        tareaDao.upsertCheckItem(itemToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        val task = tareaDao.getTareaById(itemToInsert.tareaId)
        task?.let { syncScheduler.scheduleSync(it.hogarId) }
        return itemToInsert.id
    }



    override suspend fun deleteCheckItem(item: TaskCheckItem) {
        tareaDao.deleteCheckItem(item.toEntity())
        val task = tareaDao.getTareaById(item.tareaId)
        task?.let { syncScheduler.scheduleSync(it.hogarId) }
    }

    override suspend fun updateTaskSyncTimestamp(taskId: String) {
        tareaDao.updateSyncTimestamp(taskId, System.currentTimeMillis())
    }

    override suspend fun updateTaskHogarSyncId(taskId: String, hogarSyncId: String) {
        // removed
    }

    override suspend fun updateCheckItemSyncTimestamp(itemId: String) {
        tareaDao.updateCheckItemSyncTimestamp(itemId, System.currentTimeMillis())
    }

    override suspend fun updateRewardSyncTimestamp(rewardId: String) {
        recompensaDao.updateSyncTimestamp(rewardId, System.currentTimeMillis())
    }

    override suspend fun updateCategorySyncTimestamp(categoryId: String) {
        tareaDao.updateCategoriaSyncTimestamp(categoryId, System.currentTimeMillis())
    }

    override suspend fun getCheckItemsToSync(hogarId: String): List<TaskCheckItem> {
        return tareaDao.getCheckItemsToSync(hogarId).map { it.toDomain() }
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: String) {
        // TODO: Refactor in Phase 4 using SyncWorker and Event-Based sourcing
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncAssignments(hogarId: String, taskId: String, taskSyncId: String) {
        // TODO: Refactor in Phase 4 using SyncWorker and Event-Based sourcing
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncCheckItems(hogarId: String, taskId: String, taskSyncId: String) {
        // TODO: Refactor in Phase 4 using SyncWorker and Event-Based sourcing
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncRewards(hogarId: String) {
        // TODO: Refactor in Phase 4 using SyncWorker and Event-Based sourcing
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncCategories(hogarId: String) {
        // TODO: Refactor in Phase 4 using SyncWorker and Event-Based sourcing
    }
}
