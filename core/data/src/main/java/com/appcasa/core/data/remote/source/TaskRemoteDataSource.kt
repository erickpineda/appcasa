package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.TaskDto
import com.appcasa.core.domain.model.Task
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
        firestore.collection("households").document(hogarId.toString()).collection("tasks")

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
}
