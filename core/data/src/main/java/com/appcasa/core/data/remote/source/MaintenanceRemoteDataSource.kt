package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.MaintenanceDto
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.MaintenanceEvent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getMaintenanceCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection(FirestoreConstants.COL_MAINTENANCE)

    suspend fun syncMaintenance(event: MaintenanceEvent) {
        val hogarSyncId = event.hogarSyncId ?: return
        val syncId = event.syncId ?: return
        getMaintenanceCollection(hogarSyncId).document(syncId)
            .set(MaintenanceDto.fromDomain(event)).await()
    }

    suspend fun deleteMaintenance(event: MaintenanceEvent) {
        val hogarSyncId = event.hogarSyncId ?: return
        val syncId = event.syncId ?: return
        getMaintenanceCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observeMaintenance(hogarSyncId: String): Flow<List<MaintenanceEvent>> = callbackFlow {
        val reg = getMaintenanceCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val events = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(MaintenanceDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(events)
        }
        awaitClose { reg.remove() }
    }
}
