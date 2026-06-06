package com.appcasa.core.data.remote

import com.appcasa.core.data.remote.model.TaskDto
import com.appcasa.core.domain.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun getTaskCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("tasks")

    suspend fun syncTask(task: Task) {
        val docRef = getTaskCollection(task.hogarId).document(task.id.toString())
        docRef.set(TaskDto.fromDomain(task)).await()
    }

    suspend fun deleteTask(task: Task) {
        getTaskCollection(task.hogarId).document(task.id.toString()).delete().await()
    }

    fun observeTasks(hogarId: Long, onTasksChanged: (List<Task>) -> Unit) {
        getTaskCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val tasks = snapshot?.documents?.mapNotNull { it.toObject(TaskDto::class.java)?.toDomain() } ?: emptyList()
            onTasksChanged(tasks)
        }
    }

    suspend fun syncHousehold(household: com.appcasa.core.domain.model.Household) {
        firestore.collection("households").document(household.id.toString())
            .set(household).await()
    }

    suspend fun syncMember(member: com.appcasa.core.domain.model.FamilyMember) {
        firestore.collection("households").document(member.hogarId.toString())
            .collection("members").document(member.id.toString())
            .set(member).await()
    }

    suspend fun syncExpense(expense: com.appcasa.core.domain.model.Expense) {
        firestore.collection("households").document(expense.hogarId.toString())
            .collection("expenses").document(expense.id.toString())
            .set(expense).await()
    }
}
