package com.appcasa.core.data.remote

import com.appcasa.core.data.remote.model.ExpenseDto
import com.appcasa.core.data.remote.model.MemberDto
import com.appcasa.core.data.remote.model.StockDto
import com.appcasa.core.data.remote.model.TaskDto
import com.appcasa.core.domain.model.Expense
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.StockItem
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

    private fun getExpenseCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("expenses")

    private fun getMemberCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("members")

    private fun getStockCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("stock")

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

    suspend fun syncExpense(expense: Expense) {
        getExpenseCollection(expense.hogarId).document(expense.id.toString())
            .set(ExpenseDto.fromDomain(expense)).await()
    }

    fun observeExpenses(hogarId: Long, onExpensesChanged: (List<Expense>) -> Unit) {
        getExpenseCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val expenses = snapshot?.documents?.mapNotNull { it.toObject(ExpenseDto::class.java)?.toDomain() } ?: emptyList()
            onExpensesChanged(expenses)
        }
    }

    suspend fun syncMember(member: FamilyMember) {
        getMemberCollection(member.hogarId).document(member.id.toString())
            .set(MemberDto.fromDomain(member)).await()
    }

    fun observeMembers(hogarId: Long, onMembersChanged: (List<FamilyMember>) -> Unit) {
        getMemberCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val members = snapshot?.documents?.mapNotNull { it.toObject(MemberDto::class.java)?.toDomain() } ?: emptyList()
            onMembersChanged(members)
        }
    }

    suspend fun syncStock(item: StockItem) {
        getStockCollection(item.hogarId).document(item.id.toString())
            .set(StockDto.fromDomain(item)).await()
    }

    fun observeStock(hogarId: Long, onStockChanged: (List<StockItem>) -> Unit) {
        getStockCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val items = snapshot?.documents?.mapNotNull { it.toObject(StockDto::class.java)?.toDomain() } ?: emptyList()
            onStockChanged(items)
        }
    }

    suspend fun syncHousehold(household: com.appcasa.core.domain.model.Household) {
        firestore.collection("households").document(household.id.toString())
            .set(household).await()
    }

    suspend fun getHouseholdByCode(code: String): com.appcasa.core.domain.model.Household? {
        val query = firestore.collection("households")
            .whereEqualTo("codigoHogar", code)
            .limit(1)
            .get().await()
        
        return query.documents.firstOrNull()?.toObject(com.appcasa.core.domain.model.Household::class.java)
    }
}
