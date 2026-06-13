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

    suspend fun syncTask(task: Task) {
        val hogarSyncId = task.hogarSyncId ?: return
        val syncId = task.syncId ?: return
        getTaskCollection(hogarSyncId).document(syncId)
            .set(TaskDto.fromDomain(task)).await()
    }

    suspend fun deleteTask(task: Task) {
        val hogarSyncId = task.hogarSyncId ?: return
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
}
