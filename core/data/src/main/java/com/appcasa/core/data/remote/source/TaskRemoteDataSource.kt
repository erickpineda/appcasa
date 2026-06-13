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
    private fun getTaskCollection(hogarId: Long) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarId.toString()).collection(FirestoreConstants.COL_TASKS)

    suspend fun syncTask(task: Task) {
        getTaskCollection(task.hogarId).document(task.id.toString())
            .set(TaskDto.fromDomain(task)).await()
    }

    suspend fun deleteTask(task: Task) {
        getTaskCollection(task.hogarId).document(task.id.toString()).delete().await()
    }

    fun observeTasks(hogarId: Long): Flow<List<Task>> = callbackFlow {
        val reg = getTaskCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val tasks = snapshot?.documents?.mapNotNull { it.toObject(TaskDto::class.java)?.toDomain() } ?: emptyList()
            trySend(tasks)
        }
        awaitClose { reg.remove() }
    }

    // Check Items
    private fun getCheckItemCollection(hogarId: Long, taskId: Long) = 
        getTaskCollection(hogarId).document(taskId.toString()).collection(FirestoreConstants.COL_CHECK_ITEMS)

    suspend fun syncCheckItem(hogarId: Long, item: TaskCheckItem) {
        getCheckItemCollection(hogarId, item.tareaId).document(item.id.toString())
            .set(TaskCheckItemDto.fromDomain(item)).await()
    }

    fun observeCheckItems(hogarId: Long, taskId: Long): Flow<List<TaskCheckItem>> = callbackFlow {
        val reg = getCheckItemCollection(hogarId, taskId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { it.toObject(TaskCheckItemDto::class.java)?.toDomain() } ?: emptyList()
            trySend(items)
        }
        awaitClose { reg.remove() }
    }

    // Assignments
    private fun getAssignmentCollection(hogarId: Long, taskId: Long) = 
        getTaskCollection(hogarId).document(taskId.toString()).collection(FirestoreConstants.COL_ASSIGNMENTS)

    suspend fun syncAssignment(hogarId: Long, item: TaskAssignment) {
        getAssignmentCollection(hogarId, item.tareaId).document(item.miembroId.toString())
            .set(TaskAssignmentDto.fromDomain(item)).await()
    }

    fun observeAssignments(hogarId: Long, taskId: Long): Flow<List<TaskAssignment>> = callbackFlow {
        val reg = getAssignmentCollection(hogarId, taskId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { it.toObject(TaskAssignmentDto::class.java)?.toDomain() } ?: emptyList()
            trySend(items)
        }
        awaitClose { reg.remove() }
    }
}
