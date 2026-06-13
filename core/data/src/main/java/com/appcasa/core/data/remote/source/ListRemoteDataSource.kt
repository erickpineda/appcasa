package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.*
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getListCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection(FirestoreConstants.COL_LISTS)

    suspend fun syncList(list: Lista) {
        val hogarSyncId = list.hogarSyncId ?: return
        val syncId = list.syncId ?: return
        getListCollection(hogarSyncId).document(syncId)
            .set(ListDto.fromDomain(list)).await()
    }

    suspend fun deleteList(list: Lista) {
        val hogarSyncId = list.hogarSyncId ?: return
        val syncId = list.syncId ?: return
        getListCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observeLists(hogarSyncId: String): Flow<List<Lista>> = callbackFlow {
        val reg = getListCollection(hogarSyncId).addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { doc -> 
                doc.toObject(ListDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    // Items
    private fun getItemCollection(hogarSyncId: String, listSyncId: String) = 
        getListCollection(hogarSyncId).document(listSyncId).collection(FirestoreConstants.COL_ITEMS)

    suspend fun syncListItem(hogarSyncId: String, item: ListaItem) {
        val listSyncId = item.listaSyncId ?: return
        val syncId = item.syncId ?: return
        getItemCollection(hogarSyncId, listSyncId).document(syncId)
            .set(ListItemDto.fromDomain(item)).await()
    }

    suspend fun deleteListItem(hogarSyncId: String, item: ListaItem) {
        val listSyncId = item.listaSyncId ?: return
        val syncId = item.syncId ?: return
        getItemCollection(hogarSyncId, listSyncId).document(syncId).delete().await()
    }

    fun observeListItems(hogarSyncId: String, listSyncId: String): Flow<List<ListaItem>> = callbackFlow {
        val reg = getItemCollection(hogarSyncId, listSyncId).addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { doc -> 
                doc.toObject(ListItemDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }
}
