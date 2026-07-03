package com.appcasa.core.data.remote.source

import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRemoteDataSource @Inject constructor(
  private val firestore: FirebaseFirestore
) {
  fun getTaskCollection(hogarId: String) = firestore
    .collection(FirestoreConstants.COL_HOUSEHOLDS)
    .document(hogarId)
    .collection(FirestoreConstants.COL_TASKS)

  suspend fun saveTask(task: Task) {
    getTaskCollection(task.hogarId).document(task.id).set(task).await()
  }

  suspend fun deleteTask(hogarId: String, taskId: String) {
    getTaskCollection(hogarId).document(taskId).delete().await()
  }

  suspend fun getTasks(hogarId: String): List<Task> {
    return getTaskCollection(hogarId).get().await().toObjects(Task::class.java)
  }
}
