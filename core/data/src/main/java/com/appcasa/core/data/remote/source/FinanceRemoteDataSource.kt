package com.appcasa.core.data.remote.source

import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.Expense
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRemoteDataSource @Inject constructor(
  private val firestore: FirebaseFirestore
) {
  fun getExpenseCollection(hogarId: String) = firestore
    .collection(FirestoreConstants.COL_HOUSEHOLDS)
    .document(hogarId)
    .collection(FirestoreConstants.COL_EXPENSES)

  suspend fun saveExpense(expense: Expense) {
    getExpenseCollection(expense.hogarId).document(expense.id).set(expense).await()
  }

  suspend fun deleteExpense(hogarId: String, expenseId: String) {
    getExpenseCollection(hogarId).document(expenseId).delete().await()
  }

  suspend fun getExpenses(hogarId: String): List<Expense> {
    return getExpenseCollection(hogarId).get().await().toObjects(Expense::class.java)
  }
}
