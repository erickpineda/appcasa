package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.ExpenseDto
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
    private fun getExpenseCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("expenses")

    suspend fun syncExpense(expense: Expense) {
        getExpenseCollection(expense.hogarId).document(expense.id.toString())
            .set(ExpenseDto.fromDomain(expense)).await()
    }

    suspend fun deleteExpense(expense: Expense) {
        getExpenseCollection(expense.hogarId).document(expense.id.toString()).delete().await()
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
}
