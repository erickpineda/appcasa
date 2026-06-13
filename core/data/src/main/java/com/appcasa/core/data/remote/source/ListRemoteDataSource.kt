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
    private fun getListCollection(hogarId: Long) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarId.toString()).collection(FirestoreConstants.COL_LISTS)

    suspend fun syncList(list: Lista) {
        getListCollection(list.hogarId).document(list.id.toString())
            .set(ListDto.fromDomain(list)).await()
    }

    suspend fun deleteList(list: Lista) {
        getListCollection(list.hogarId).document(list.id.toString()).delete().await()
    }

    fun observeLists(hogarId: Long): Flow<List<Lista>> = callbackFlow {
        val reg = getListCollection(hogarId).addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(ListDto::class.java)?.toDomain() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    // Items
    private fun getItemCollection(hogarId: Long, listaId: Long) = 
        getListCollection(hogarId).document(listaId.toString()).collection(FirestoreConstants.COL_ITEMS)

    suspend fun syncListItem(hogarId: Long, item: ListaItem) {
        getItemCollection(hogarId, item.listaId).document(item.id.toString())
            .set(ListItemDto.fromDomain(item)).await()
    }

    suspend fun deleteListItem(hogarId: Long, item: ListaItem) {
        getItemCollection(hogarId, item.listaId).document(item.id.toString()).delete().await()
    }

    fun observeListItems(hogarId: Long, listaId: Long): Flow<List<ListaItem>> = callbackFlow {
        val reg = getItemCollection(hogarId, listaId).addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(ListItemDto::class.java)?.toDomain() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }
}
