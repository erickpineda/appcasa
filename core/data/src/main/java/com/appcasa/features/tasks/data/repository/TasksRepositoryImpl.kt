package com.appcasa.features.tasks.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.TaskRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.TasksRepository
import com.appcasa.features.tasks.data.local.RecompensaDao
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.mapper.toDomain
import com.appcasa.features.tasks.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class TasksRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val tareaDao: TareaDao,
    private val recompensaDao: RecompensaDao,
    private val syncScheduler: SyncScheduler,
    private val syncManager: SyncManager,
    private val remoteDataSource: TaskRemoteDataSource
) : TasksRepository {

    override fun getTasksByHogar(hogarId: Long): Flow<List<Task>> {
        return tareaDao.getTareasByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTasksPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Task>> {
        return tareaDao.getTareasPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedTasksPaged(hogarId: Long, limit: Int, offset: Int): Flow<List<Task>> {
        return tareaDao.getArchivedTareasPaged(hogarId, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTaskById(taskId: Long): Flow<Task?> {
        return kotlinx.coroutines.flow.flow {
            emit(tareaDao.getTareaById(taskId)?.toDomain())
        }
    }

    override suspend fun insertTask(task: Task): Long {
        val id = tareaDao.insertTarea(task.toEntity())
        syncScheduler.scheduleSync(task.hogarId)
        return id
    }

    override suspend fun deleteTask(task: Task) {
        tareaDao.deleteTarea(task.toEntity())
        syncScheduler.scheduleSync(task.hogarId)
    }

    override suspend fun updateTaskStatus(taskId: Long, status: EstadoTarea) {
        val taskEntity = tareaDao.getTareaById(taskId)
        taskEntity?.let {
            tareaDao.updateTarea(it.copy(estado = status.name, updatedAt = System.currentTimeMillis()))
            syncScheduler.scheduleSync(it.hogarId)
        }
    }

    override suspend fun unarchiveTarea(id: Long) {
        tareaDao.unarchiveTarea(id)
    }

    override suspend fun archiveOldCompletedTasks(hogarId: Long, threshold: Long) {
        tareaDao.archiveOldCompletedTasks(hogarId, threshold)
    }

    override suspend fun deleteAllArchivedTasks(hogarId: Long) {
        tareaDao.deleteAllArchivedTasks(hogarId)
    }

    override fun getAllCheckItemsCounts(hogarId: Long): Flow<Map<Long, Pair<Int, Int>>> {
        return tareaDao.getAllCheckItemsCounts(hogarId).map { list ->
            list.associate { it.taskId to (it.total to it.completed) }
        }
    }

    override fun getRewardsByHogar(hogarId: Long): Flow<List<Reward>> {
        return recompensaDao.getRecompensasByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertReward(reward: Reward) {
        recompensaDao.insertRecompensa(reward.toEntity())
    }

    override suspend fun deleteReward(reward: Reward) {
        recompensaDao.deleteRecompensa(reward.toEntity())
    }

    override fun getCategoriesByHogar(hogarId: Long): Flow<List<TaskCategory>> {
        return tareaDao.getCategorias(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCategory(category: TaskCategory) {
        tareaDao.insertCategoria(category.toEntity())
    }

    override fun getAssignmentsForTask(taskId: Long): Flow<List<TaskAssignment>> {
        return kotlinx.coroutines.flow.flow {
             val assignment = tareaDao.getAsignacionByTarea(taskId)
             emit(assignment?.let { listOf(it.toDomain()) } ?: emptyList())
        }
    }

    override suspend fun insertAssignment(assignment: TaskAssignment) {
        tareaDao.insertAsignacion(assignment.toEntity())
    }

    override fun getCheckItemsForTask(taskId: Long): Flow<List<TaskCheckItem>> {
        return tareaDao.getCheckItems(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCheckItem(item: TaskCheckItem): Long {
        return tareaDao.insertCheckItem(item.toEntity())
    }

    override suspend fun updateCheckItem(item: TaskCheckItem) {
        tareaDao.updateCheckItem(item.toEntity())
    }

    override suspend fun deleteCheckItem(item: TaskCheckItem) {
        tareaDao.deleteCheckItem(item.toEntity())
    }

    override suspend fun updateTaskSyncTimestamp(taskId: Long) {
        tareaDao.updateSyncTimestamp(taskId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    remoteDataSource.observeTasks(hogarId)
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteTasks ->
                remoteTasks.forEach { remoteTask ->
                    val localTask = tareaDao.getTareaById(remoteTask.id)
                    if (localTask == null || remoteTask.updatedAt > localTask.updatedAt) {
                        tareaDao.insertTarea(remoteTask.toEntity())
                    }
                }
            }
            .launchIn(appScope)
    }
}
