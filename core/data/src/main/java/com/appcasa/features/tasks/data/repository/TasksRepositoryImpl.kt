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
        return flow {
            emit(tareaDao.getTareaById(taskId)?.toDomain())
        }
    }

    override suspend fun insertTask(task: Task): Long {
        var taskToInsert = task
        if (taskToInsert.hogarSyncId == null && taskToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(taskToInsert.hogarId).first()
            taskToInsert = taskToInsert.copy(hogarSyncId = hogar?.syncId)
        }

        if (taskToInsert.syncId == null) {
            taskToInsert = taskToInsert.copy(syncId = UUID.randomUUID().toString())
        }

        val existing = taskToInsert.syncId?.let { tareaDao.getTareaBySyncId(it) }
        if (existing != null) {
            taskToInsert = taskToInsert.copy(id = existing.id)
        }

        val id = tareaDao.insertTarea(taskToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(taskToInsert.hogarId)
        return id
    }

    override suspend fun deleteTask(task: Task) {
        tareaDao.deleteTarea(task.toEntity())
        try {
            val hogar = householdRepository.getHogarById(task.hogarId).first()
            val hSyncId = task.hogarSyncId ?: hogar?.syncId
            if (hSyncId != null) {
                remoteDataSource.deleteTask(hSyncId, task)
            }
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
            
            if (status == EstadoTarea.COMPLETADA && oldStatus != EstadoTarea.COMPLETADA.name && !it.puntosOtorgados) {
                val assignments = tareaDao.getAsignacionesByTarea(taskId)
                assignments.forEach { assignment ->
                    familyRepository.addPointsToMember(assignment.miembroId, it.points)
                }
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
        var rewardToInsert = reward
        if (rewardToInsert.hogarSyncId == null && rewardToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(rewardToInsert.hogarId).first()
            rewardToInsert = rewardToInsert.copy(hogarSyncId = hogar?.syncId)
        }
        if (rewardToInsert.syncId == null) {
            rewardToInsert = rewardToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = rewardToInsert.syncId?.let { recompensaDao.getRecompensaBySyncId(it) }
        if (existing != null) {
            rewardToInsert = rewardToInsert.copy(id = existing.id)
        }
        recompensaDao.insertRecompensa(rewardToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(rewardToInsert.hogarId)
    }

    override suspend fun deleteReward(reward: Reward) {
        recompensaDao.deleteRecompensa(reward.toEntity())
        try {
            val hogar = householdRepository.getHogarById(reward.hogarId).first()
            val hSyncId = reward.hogarSyncId ?: hogar?.syncId
            if (hSyncId != null) {
                remoteDataSource.deleteReward(hSyncId, reward)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncScheduler.scheduleSync(reward.hogarId)
    }

    override fun getCategoriesByHogar(hogarId: Long): Flow<List<TaskCategory>> {
        return tareaDao.getCategorias(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCategory(category: TaskCategory) {
        var categoryToInsert = category
        if (categoryToInsert.hogarSyncId == null && categoryToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(categoryToInsert.hogarId).first()
            categoryToInsert = categoryToInsert.copy(hogarSyncId = hogar?.syncId)
        }
        if (categoryToInsert.syncId == null) {
            categoryToInsert = categoryToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = categoryToInsert.syncId?.let { tareaDao.getCategoriaBySyncId(it) }
        if (existing != null) {
            categoryToInsert = categoryToInsert.copy(id = existing.id)
        }
        tareaDao.insertCategoria(categoryToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(categoryToInsert.hogarId)
    }

    override fun getAssignmentsForTask(taskId: Long): Flow<List<TaskAssignment>> {
        return flow {
             val assignment = tareaDao.getAsignacionByTarea(taskId)
             emit(assignment?.let { listOf(it.toDomain()) } ?: emptyList())
        }
    }

    override suspend fun insertAssignment(assignment: TaskAssignment) {
        val task = tareaDao.getTareaById(assignment.tareaId)
        val member = familyRepository.getMemberById(assignment.miembroId)
        
        val assignmentToInsert = assignment.copy(
            syncId = assignment.syncId ?: "${task?.syncId}_${member?.syncId}",
            tareaSyncId = task?.syncId,
            miembroSyncId = member?.syncId
        )
        tareaDao.insertAsignacion(assignmentToInsert.toEntity())
    }

    override fun getCheckItemsForTask(taskId: Long): Flow<List<TaskCheckItem>> {
        return tareaDao.getCheckItems(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCheckItem(item: TaskCheckItem): Long {
        var itemToInsert = item
        if (itemToInsert.syncId == null) {
            itemToInsert = itemToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val id = tareaDao.insertCheckItem(itemToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        val task = tareaDao.getTareaById(itemToInsert.tareaId)
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

    override suspend fun updateTaskHogarSyncId(taskId: Long, hogarSyncId: String) {
        tareaDao.updateHogarSyncId(taskId, hogarSyncId)
    }

    override suspend fun updateCheckItemSyncTimestamp(itemId: Long) {
        tareaDao.updateCheckItemSyncTimestamp(itemId, System.currentTimeMillis())
    }

    override suspend fun updateRewardSyncTimestamp(rewardId: Long) {
        recompensaDao.updateSyncTimestamp(rewardId, System.currentTimeMillis())
    }

    override suspend fun updateCategorySyncTimestamp(categoryId: Long) {
        tareaDao.updateCategoriaSyncTimestamp(categoryId, System.currentTimeMillis())
    }

    override suspend fun getCheckItemsToSync(hogarId: Long): List<TaskCheckItem> {
        return tareaDao.getCheckItemsToSync(hogarId).map { it.toDomain() }
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeTasks(it) } ?: emptyFlow()
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteTasks ->
                remoteTasks.forEach { remoteTask ->
                    val rSyncId = remoteTask.syncId ?: return@forEach
                    val existing = tareaDao.getTareaBySyncId(rSyncId)
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    
                    val taskToSave = remoteTask.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId
                    )

                    if (existing == null) {
                        tareaDao.insertTarea(taskToSave.toEntity())
                        NotificationHelper.showNotification(
                            context,
                            rSyncId.hashCode(),
                            context.getString(R.string.notif_new_task_title),
                            taskToSave.titulo
                        )
                    } else if (remoteTask.updatedAt > existing.updatedAt) {
                        if (remoteTask.estado == EstadoTarea.COMPLETADA && existing.estado != EstadoTarea.COMPLETADA.name) {
                            NotificationHelper.showNotification(
                                context,
                                rSyncId.hashCode(),
                                context.getString(R.string.notif_task_completed_title),
                                context.getString(R.string.notif_task_completed_msg, taskToSave.titulo)
                            )
                        }
                        tareaDao.insertTarea(taskToSave.toEntity())
                    }
                    
                    observeAndSyncCheckItems(hogarId, taskToSave.id, rSyncId) 
                    observeAndSyncAssignments(hogarId, taskToSave.id, rSyncId)
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)

        observeAndSyncRewards(hogarId)
        observeAndSyncCategories(hogarId)
    }

    private val assignmentSyncJobs = mutableMapOf<Long, Job>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncAssignments(hogarId: Long, taskId: Long, taskSyncId: String) {
        assignmentSyncJobs[taskId]?.cancel()
        
        assignmentSyncJobs[taskId] = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeAssignments(it, taskSyncId) } ?: emptyFlow()
                }
                else emptyFlow()
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val memberSyncId = remoteItem.miembroSyncId ?: return@forEach
                    val member = familyRepository.getMemberBySyncId(memberSyncId)
                    if (member != null) {
                        val localItem = tareaDao.getAsignacionByTareaAndMiembro(taskId, member.id)
                        if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
                            tareaDao.insertAsignacion(remoteItem.copy(tareaId = taskId, miembroId = member.id).toEntity())
                        }
                    }
                }
            }
            .catch { e -> 
                e.printStackTrace()
                assignmentSyncJobs.remove(taskId)
            }
            .onCompletion {
                assignmentSyncJobs.remove(taskId)
            }
            .launchIn(appScope)
    }

    private val checkItemSyncJobs = mutableMapOf<Long, Job>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncCheckItems(hogarId: Long, taskId: Long, taskSyncId: String) {
        checkItemSyncJobs[taskId]?.cancel()
        
        checkItemSyncJobs[taskId] = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeCheckItems(it, taskSyncId) } ?: emptyFlow()
                }
                else emptyFlow()
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val existing = remoteItem.syncId?.let { tareaDao.getCheckItemBySyncId(it) }
                    val itemToSave = remoteItem.copy(
                        id = existing?.id ?: 0L,
                        tareaId = taskId,
                        tareaSyncId = taskSyncId
                    )
                    if (existing == null || remoteItem.updatedAt > existing.updatedAt) {
                        tareaDao.insertCheckItem(itemToSave.toEntity())
                    }
                }
            }
            .catch { e -> 
                e.printStackTrace()
                checkItemSyncJobs.remove(taskId)
            }
            .onCompletion {
                checkItemSyncJobs.remove(taskId)
            }
            .launchIn(appScope)
    }

    private var rewardSyncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncRewards(hogarId: Long) {
        rewardSyncJob?.cancel()
        rewardSyncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeRewards(it) } ?: emptyFlow()
                } else emptyFlow()
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val existing = remoteItem.syncId?.let { recompensaDao.getRecompensaBySyncId(it) }
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    
                    val itemToSave = remoteItem.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId,
                        lastSyncedAt = System.currentTimeMillis()
                    )

                    if (existing == null || remoteItem.updatedAt > existing.updatedAt) {
                        recompensaDao.insertRecompensa(itemToSave.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }

    private var categorySyncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndSyncCategories(hogarId: Long) {
        categorySyncJob?.cancel()
        categorySyncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeCategories(it) } ?: emptyFlow()
                } else emptyFlow()
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val existing = remoteItem.syncId?.let { tareaDao.getCategoriaBySyncId(it) }
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    
                    val itemToSave = remoteItem.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId,
                        lastSyncedAt = System.currentTimeMillis()
                    )

                    if (existing == null || remoteItem.updatedAt > existing.updatedAt) {
                        tareaDao.insertCategoria(itemToSave.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
