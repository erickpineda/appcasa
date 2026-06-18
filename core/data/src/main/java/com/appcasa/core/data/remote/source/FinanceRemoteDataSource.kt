package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.ExpenseDto
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.Expense
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getExpenseCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection(FirestoreConstants.COL_EXPENSES)

    suspend fun syncExpense(hogarSyncId: String, expense: Expense) {
        val syncId = expense.syncId ?: return
        val dto = ExpenseDto.fromDomain(expense).copy(hogarSyncId = hogarSyncId)
        getExpenseCollection(hogarSyncId).document(syncId)
            .set(dto).await()
    }

    suspend fun deleteExpense(hogarSyncId: String, expense: Expense) {
        val syncId = expense.syncId ?: return
        getExpenseCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observeExpenses(hogarSyncId: String): Flow<List<Expense>> = callbackFlow {
        val reg = getExpenseCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val expenses = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(ExpenseDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(expenses)
        }
        awaitClose { reg.remove() }
    }
}
