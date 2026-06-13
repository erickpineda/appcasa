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
    private fun getEventCollection(hogarId: Long) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarId.toString()).collection(FirestoreConstants.COL_EVENTS)

    suspend fun syncEvent(event: Event) {
        getEventCollection(event.hogarId).document(event.id.toString())
            .set(EventDto.fromDomain(event)).await()
    }

    suspend fun deleteEvent(event: Event) {
        getEventCollection(event.hogarId).document(event.id.toString()).delete().await()
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
}
