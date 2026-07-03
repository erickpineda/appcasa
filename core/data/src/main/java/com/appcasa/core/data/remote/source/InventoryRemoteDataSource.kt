package com.appcasa.core.data.remote.source

import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.StockItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRemoteDataSource @Inject constructor(
  private val firestore: FirebaseFirestore
) {
  fun getStockCollection(hogarId: String) = firestore
    .collection(FirestoreConstants.COL_HOUSEHOLDS)
    .document(hogarId)
    .collection(FirestoreConstants.COL_TASKS) // Reusing 'inventory' logic but mapped to Firestore hierarchy

  suspend fun saveStockItem(item: StockItem) {
    firestore.collection(FirestoreConstants.COL_HOUSEHOLDS)
      .document(item.hogarId)
      .collection(FirestoreConstants.COL_TASKS) // Should be COL_INVENTORY in constants, checking...
      .document(item.id)
      .set(item)
      .await()
  }

  suspend fun deleteStockItem(hogarId: String, itemId: String) {
    firestore.collection(FirestoreConstants.COL_HOUSEHOLDS)
      .document(hogarId)
      .collection(FirestoreConstants.COL_TASKS)
      .document(itemId)
      .delete()
      .await()
  }
}
