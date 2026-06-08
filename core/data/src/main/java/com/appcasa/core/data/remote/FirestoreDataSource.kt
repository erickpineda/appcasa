package com.appcasa.core.data.remote

import com.appcasa.core.data.remote.model.EventDto
import com.appcasa.core.data.remote.model.ExpenseDto
import com.appcasa.core.data.remote.model.MemberDto
import com.appcasa.core.data.remote.model.PostItDto
import com.appcasa.core.data.remote.model.StockDto
import com.appcasa.core.data.remote.model.TaskDto
import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.model.Expense
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.PostIt
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

    private fun getPostItCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("postits")

    private fun getEventCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("events")

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

    suspend fun syncPostIt(postIt: PostIt) {
        getPostItCollection(postIt.hogarId).document(postIt.id.toString())
            .set(PostItDto.fromDomain(postIt)).await()
    }

    fun observePostIts(hogarId: Long, onPostItsChanged: (List<PostIt>) -> Unit) {
        getPostItCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val postIts = snapshot?.documents?.mapNotNull { it.toObject(PostItDto::class.java)?.toDomain() } ?: emptyList()
            onPostItsChanged(postIts)
        }
    }

    suspend fun syncEvent(event: Event) {
        getEventCollection(event.hogarId).document(event.id.toString())
            .set(EventDto.fromDomain(event)).await()
    }

    fun observeEvents(hogarId: Long, onEventsChanged: (List<Event>) -> Unit) {
        getEventCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val events = snapshot?.documents?.mapNotNull { it.toObject(EventDto::class.java)?.toDomain() } ?: emptyList()
            onEventsChanged(events)
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
