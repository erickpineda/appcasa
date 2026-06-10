package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.MaintenanceDto
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
    private fun getMaintenanceCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("maintenance")

    suspend fun syncMaintenance(event: MaintenanceEvent) {
        getMaintenanceCollection(event.hogarId).document(event.id.toString())
            .set(MaintenanceDto.fromDomain(event)).await()
    }

    suspend fun deleteMaintenance(event: MaintenanceEvent) {
        getMaintenanceCollection(event.hogarId).document(event.id.toString()).delete().await()
    }

    fun observeMaintenance(hogarId: Long): Flow<List<MaintenanceEvent>> = callbackFlow {
        val reg = getMaintenanceCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val events = snapshot?.documents?.mapNotNull { it.toObject(MaintenanceDto::class.java)?.toDomain() } ?: emptyList()
            trySend(events)
        }
        awaitClose { reg.remove() }
    }
}
