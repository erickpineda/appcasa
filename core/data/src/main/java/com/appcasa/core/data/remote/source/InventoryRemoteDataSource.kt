package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.StockDto
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.StockItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getStockCollection(hogarId: Long) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarId.toString()).collection(FirestoreConstants.COL_ITEMS)

    suspend fun syncStock(item: StockItem) {
        getStockCollection(item.hogarId).document(item.id.toString())
            .set(StockDto.fromDomain(item)).await()
    }

    suspend fun deleteStock(item: StockItem) {
        getStockCollection(item.hogarId).document(item.id.toString()).delete().await()
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
}
