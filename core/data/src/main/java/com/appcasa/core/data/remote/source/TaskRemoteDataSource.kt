package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.TaskAssignmentDto
import com.appcasa.core.data.remote.model.TaskCheckItemDto
import com.appcasa.core.data.remote.model.TaskDto
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.Task
import com.appcasa.core.domain.model.TaskAssignment
import com.appcasa.core.domain.model.TaskCheckItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getTaskCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection(FirestoreConstants.COL_TASKS)

    suspend fun syncTask(hogarSyncId: String, task: Task) {
        val syncId = task.syncId ?: return
        val dto = TaskDto.fromDomain(task).copy(hogarSyncId = hogarSyncId)
        getTaskCollection(hogarSyncId).document(syncId)
            .set(dto).await()
    }

    suspend fun deleteTask(hogarSyncId: String, task: Task) {
        val syncId = task.syncId ?: return
        getTaskCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observeTasks(hogarSyncId: String): Flow<List<Task>> = callbackFlow {
        val reg = getTaskCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val tasks = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(TaskDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(tasks)
        }
        awaitClose { reg.remove() }
    }

    // Check Items
    private fun getCheckItemCollection(hogarSyncId: String, taskSyncId: String) = 
        getTaskCollection(hogarSyncId).document(taskSyncId).collection(FirestoreConstants.COL_CHECK_ITEMS)

    suspend fun syncCheckItem(hogarSyncId: String, item: TaskCheckItem) {
        val taskSyncId = item.tareaSyncId ?: return
        val syncId = item.syncId ?: return
        getCheckItemCollection(hogarSyncId, taskSyncId).document(syncId)
            .set(TaskCheckItemDto.fromDomain(item)).await()
    }

    fun observeCheckItems(hogarSyncId: String, taskSyncId: String): Flow<List<TaskCheckItem>> = callbackFlow {
        val reg = getCheckItemCollection(hogarSyncId, taskSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(TaskCheckItemDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { reg.remove() }
    }

    // Assignments
    private fun getAssignmentCollection(hogarSyncId: String, taskSyncId: String) = 
        getTaskCollection(hogarSyncId).document(taskSyncId).collection(FirestoreConstants.COL_ASSIGNMENTS)

    suspend fun syncAssignment(hogarSyncId: String, item: TaskAssignment) {
        val taskSyncId = item.tareaSyncId ?: return
        val memberSyncId = item.miembroSyncId ?: return
        val syncId = item.syncId ?: "${taskSyncId}_${memberSyncId}"
        getAssignmentCollection(hogarSyncId, taskSyncId).document(syncId)
            .set(TaskAssignmentDto.fromDomain(item.copy(syncId = syncId))).await()
    }

    fun observeAssignments(hogarSyncId: String, taskSyncId: String): Flow<List<TaskAssignment>> = callbackFlow {
        val reg = getAssignmentCollection(hogarSyncId, taskSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(TaskAssignmentDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { reg.remove() }
    }

    // Rewards
    private fun getRewardCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection("rewards")

    suspend fun syncReward(hogarSyncId: String, reward: com.appcasa.core.domain.model.Reward) {
        val syncId = reward.syncId ?: return
        val dto = com.appcasa.core.data.remote.model.RewardDto.fromDomain(reward).copy(hogarSyncId = hogarSyncId)
        getRewardCollection(hogarSyncId).document(syncId)
            .set(dto).await()
    }

    suspend fun deleteReward(hogarSyncId: String, reward: com.appcasa.core.domain.model.Reward) {
        val syncId = reward.syncId ?: return
        getRewardCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observeRewards(hogarSyncId: String): Flow<List<com.appcasa.core.domain.model.Reward>> = callbackFlow {
        val reg = getRewardCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val rewards = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(com.appcasa.core.data.remote.model.RewardDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(rewards)
        }
        awaitClose { reg.remove() }
    }

    // Task Categories
    private fun getCategoryCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection("task_categories")

    suspend fun syncCategory(hogarSyncId: String, category: com.appcasa.core.domain.model.TaskCategory) {
        val syncId = category.syncId ?: return
        val dto = com.appcasa.core.data.remote.model.TaskCategoryDto.fromDomain(category).copy(hogarSyncId = hogarSyncId)
        getCategoryCollection(hogarSyncId).document(syncId)
            .set(dto).await()
    }

    suspend fun deleteCategory(hogarSyncId: String, category: com.appcasa.core.domain.model.TaskCategory) {
        val syncId = category.syncId ?: return
        getCategoryCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observeCategories(hogarSyncId: String): Flow<List<com.appcasa.core.domain.model.TaskCategory>> = callbackFlow {
        val reg = getCategoryCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val categories = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(com.appcasa.core.data.remote.model.TaskCategoryDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(categories)
        }
        awaitClose { reg.remove() }
    }
}
