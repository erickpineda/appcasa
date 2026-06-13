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
    private fun getStockCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection(FirestoreConstants.COL_ITEMS)

    suspend fun syncStock(item: StockItem) {
        val hogarSyncId = item.hogarSyncId ?: return
        val syncId = item.syncId ?: return
        getStockCollection(hogarSyncId).document(syncId)
            .set(StockDto.fromDomain(item)).await()
    }

    suspend fun deleteStock(item: StockItem) {
        val hogarSyncId = item.hogarSyncId ?: return
        val syncId = item.syncId ?: return
        getStockCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observeStock(hogarSyncId: String): Flow<List<StockItem>> = callbackFlow {
        val reg = getStockCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(StockDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { reg.remove() }
    }
}
