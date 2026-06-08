package com.appcasa.core.data.remote

import com.appcasa.core.data.remote.model.EventDto
import com.appcasa.core.data.remote.model.ExpenseDto
import com.appcasa.core.data.remote.model.HouseholdDto
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    suspend fun syncExpense(expense: Expense) {
        getExpenseCollection(expense.hogarId).document(expense.id.toString())
            .set(ExpenseDto.fromDomain(expense)).await()
    }

    fun observeExpenses(hogarId: Long): Flow<List<Expense>> = callbackFlow {
        val reg = getExpenseCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val expenses = snapshot?.documents?.mapNotNull { it.toObject(ExpenseDto::class.java)?.toDomain() } ?: emptyList()
            trySend(expenses)
        }
        awaitClose { reg.remove() }
    }

    suspend fun syncMember(member: FamilyMember) {
        getMemberCollection(member.hogarId).document(member.id.toString())
            .set(MemberDto.fromDomain(member)).await()
    }

    fun observeMembers(hogarId: Long): Flow<List<FamilyMember>> = callbackFlow {
        val reg = getMemberCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val members = snapshot?.documents?.mapNotNull { it.toObject(MemberDto::class.java)?.toDomain() } ?: emptyList()
            trySend(members)
        }
        awaitClose { reg.remove() }
    }

    suspend fun syncStock(item: StockItem) {
        getStockCollection(item.hogarId).document(item.id.toString())
            .set(StockDto.fromDomain(item)).await()
    }

    fun observeStock(hogarId: Long): Flow<List<StockItem>> = callbackFlow {
        val reg = getStockCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { it.toObject(StockDto::class.java)?.toDomain() } ?: emptyList()
            trySend(items)
        }
        awaitClose { reg.remove() }
    }

    suspend fun syncPostIt(postIt: PostIt) {
        getPostItCollection(postIt.hogarId).document(postIt.id.toString())
            .set(PostItDto.fromDomain(postIt)).await()
    }

    fun observePostIts(hogarId: Long): Flow<List<PostIt>> = callbackFlow {
        val reg = getPostItCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val postIts = snapshot?.documents?.mapNotNull { it.toObject(PostItDto::class.java)?.toDomain() } ?: emptyList()
            trySend(postIts)
        }
        awaitClose { reg.remove() }
    }

    suspend fun syncEvent(event: Event) {
        getEventCollection(event.hogarId).document(event.id.toString())
            .set(EventDto.fromDomain(event)).await()
    }

    fun observeEvents(hogarId: Long): Flow<List<Event>> = callbackFlow {
        val reg = getEventCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val events = snapshot?.documents?.mapNotNull { it.toObject(EventDto::class.java)?.toDomain() } ?: emptyList()
            trySend(events)
        }
        awaitClose { reg.remove() }
    }

    suspend fun syncHousehold(household: com.appcasa.core.domain.model.Household) {
        firestore.collection("households").document(household.id.toString())
            .set(HouseholdDto.fromDomain(household)).await()
    }

    suspend fun getHouseholdByCode(code: String): com.appcasa.core.domain.model.Household? {
        val query = firestore.collection("households")
            .whereEqualTo("codigoHogar", code)
            .limit(1)
            .get().await()
        
        return query.documents.firstOrNull()?.toObject(HouseholdDto::class.java)?.toDomain()
    }
}
