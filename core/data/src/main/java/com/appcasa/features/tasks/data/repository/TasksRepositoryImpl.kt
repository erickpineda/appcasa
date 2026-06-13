package com.appcasa.features.tasks.data.repository

import com.appcasa.core.data.R
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.TaskRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.TasksRepository
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

class TasksRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val tareaDao: TareaDao,
    private val recompensaDao: RecompensaDao,
    private val familyRepository: FamilyRepository,
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
        val id = tareaDao.insertTarea(task.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(task.hogarId)
        return id
    }

    override suspend fun deleteTask(task: Task) {
        tareaDao.deleteTarea(task.toEntity())
        try {
            remoteDataSource.deleteTask(task)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncScheduler.scheduleSync(task.hogarId)
    }

    override suspend fun updateTaskStatus(taskId: Long, status: EstadoTarea) {
        val taskEntity = tareaDao.getTareaById(taskId)
        taskEntity?.let {
            val oldStatus = it.estado
            tareaDao.updateTarea(it.copy(estado = status.name, updatedAt = System.currentTimeMillis()))
            
            // Si la tarea se completa y no se han otorgado puntos todavía
            if (status == EstadoTarea.COMPLETADA && oldStatus != EstadoTarea.COMPLETADA.name && !it.puntosOtorgados) {
                val assignments = tareaDao.getAsignacionesByTarea(taskId)
                assignments.forEach { assignment ->
                    familyRepository.addPointsToMember(assignment.miembroId, it.points)
                }
                // Marcamos que los puntos ya han sido otorgados para esta tarea
                tareaDao.updateTarea(it.copy(estado = status.name, puntosOtorgados = true, updatedAt = System.currentTimeMillis()))
            }

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
        val id = tareaDao.insertCheckItem(item.copy(updatedAt = System.currentTimeMillis()).toEntity())
        val task = tareaDao.getTareaById(item.tareaId)
        task?.let { syncScheduler.scheduleSync(it.hogarId) }
        return id
    }

    override suspend fun updateCheckItem(item: TaskCheckItem) {
        tareaDao.updateCheckItem(item.copy(updatedAt = System.currentTimeMillis()).toEntity())
        val task = tareaDao.getTareaById(item.tareaId)
        task?.let { syncScheduler.scheduleSync(it.hogarId) }
    }

    override suspend fun deleteCheckItem(item: TaskCheckItem) {
        tareaDao.deleteCheckItem(item.toEntity())
        val task = tareaDao.getTareaById(item.tareaId)
        task?.let { syncScheduler.scheduleSync(it.hogarId) }
    }

    override suspend fun updateTaskSyncTimestamp(taskId: Long) {
        tareaDao.updateSyncTimestamp(taskId, System.currentTimeMillis())
    }

    override suspend fun updateCheckItemSyncTimestamp(itemId: Long) {
        tareaDao.updateCheckItemSyncTimestamp(itemId, System.currentTimeMillis())
    }

    override suspend fun getCheckItemsToSync(hogarId: Long): List<TaskCheckItem> {
        return tareaDao.getCheckItemsToSync(hogarId).map { it.toDomain() }
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
                val remoteIds = remoteTasks.map { it.id }.toSet()
                val localTasks = tareaDao.getTareasByHogar(hogarId).first()
                
                localTasks.forEach { local ->
                    if (local.id !in remoteIds) {
                        tareaDao.deleteTarea(local)
                    }
                }

                remoteTasks.forEach { remoteTask ->
                    val localTask = tareaDao.getTareaById(remoteTask.id)
                    if (localTask == null) {
                        tareaDao.insertTarea(remoteTask.toEntity())
                        NotificationHelper.showNotification(
                            context,
                            remoteTask.id.toInt() + 3000,
                            context.getString(R.string.notif_new_task_title),
                            remoteTask.titulo
                        )
                    } else {
                        if (remoteTask.estado == EstadoTarea.COMPLETADA && localTask.estado != EstadoTarea.COMPLETADA.name) {
                            NotificationHelper.showNotification(
                                context,
                                remoteTask.id.toInt() + 4000,
                                context.getString(R.string.notif_task_completed_title),
                                context.getString(R.string.notif_task_completed_msg, remoteTask.titulo)
                            )
                        }
                        
                        if (remoteTask.updatedAt > localTask.updatedAt) {
                            tareaDao.insertTarea(remoteTask.toEntity())
                        }
                    }
                    
                    // Sincronizar sub-items
                    observeAndSyncCheckItems(hogarId, remoteTask.id)
                    observeAndSyncAssignments(hogarId, remoteTask.id)
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }

    private val assignmentSyncJobs = mutableMapOf<Long, Job>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncAssignments(hogarId: Long, taskId: Long) {
        if (assignmentSyncJobs.containsKey(taskId)) return
        
        assignmentSyncJobs[taskId] = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) remoteDataSource.observeAssignments(hogarId, taskId)
                else emptyFlow()
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val localItem = tareaDao.getAsignacionByTareaAndMiembro(remoteItem.tareaId, remoteItem.miembroId)
                    if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
                        tareaDao.insertAsignacion(remoteItem.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }

    private val checkItemSyncJobs = mutableMapOf<Long, Job>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncCheckItems(hogarId: Long, taskId: Long) {
        if (checkItemSyncJobs.containsKey(taskId)) return
        
        checkItemSyncJobs[taskId] = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) remoteDataSource.observeCheckItems(hogarId, taskId)
                else emptyFlow()
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val localItem = tareaDao.getCheckItemById(remoteItem.id)
                    if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
                        tareaDao.insertCheckItem(remoteItem.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
