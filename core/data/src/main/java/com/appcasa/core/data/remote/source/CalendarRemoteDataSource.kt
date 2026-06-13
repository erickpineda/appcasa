package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.EventDto
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getEventCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection(FirestoreConstants.COL_EVENTS)

    suspend fun syncEvent(event: Event) {
        val hogarSyncId = event.hogarSyncId ?: return
        val syncId = event.syncId ?: return
        getEventCollection(hogarSyncId).document(syncId)
            .set(EventDto.fromDomain(event)).await()
    }

    suspend fun deleteEvent(event: Event) {
        val hogarSyncId = event.hogarSyncId ?: return
        val syncId = event.syncId ?: return
        getEventCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observeEvents(hogarSyncId: String): Flow<List<Event>> = callbackFlow {
        val reg = getEventCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val events = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(EventDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(events)
        }
        awaitClose { reg.remove() }
    }
}
