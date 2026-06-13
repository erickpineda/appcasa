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
class PetRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getPetCollection(hogarSyncId: String, mascotaSyncId: String, sub: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId)
            .collection(FirestoreConstants.COL_MEMBERS).document(mascotaSyncId)
            .collection(sub)

    // Pesos
    suspend fun syncWeight(hogarSyncId: String, weight: PetWeight) {
        val mascotaSyncId = weight.mascotaSyncId ?: return
        val syncId = weight.syncId ?: return
        getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_WEIGHTS).document(syncId)
            .set(PetWeightDto.fromDomain(weight)).await()
    }
    fun observeWeights(hogarSyncId: String, mascotaSyncId: String): Flow<List<PetWeight>> = callbackFlow {
        val reg = getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_WEIGHTS).addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { doc -> 
                doc.toObject(PetWeightDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }
    suspend fun deleteWeight(hogarSyncId: String, weight: PetWeight) {
        val mascotaSyncId = weight.mascotaSyncId ?: return
        val syncId = weight.syncId ?: return
        getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_WEIGHTS).document(syncId).delete().await()
    }

    // Vacunas
    suspend fun syncVaccine(hogarSyncId: String, vaccine: PetVaccine) {
        val mascotaSyncId = vaccine.mascotaSyncId ?: return
        val syncId = vaccine.syncId ?: return
        getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_VACCINES).document(syncId)
            .set(PetVaccineDto.fromDomain(vaccine)).await()
    }
    fun observeVaccines(hogarSyncId: String, mascotaSyncId: String): Flow<List<PetVaccine>> = callbackFlow {
        val reg = getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_VACCINES).addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { doc -> 
                doc.toObject(PetVaccineDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }
    suspend fun deleteVaccine(hogarSyncId: String, vaccine: PetVaccine) {
        val mascotaSyncId = vaccine.mascotaSyncId ?: return
        val syncId = vaccine.syncId ?: return
        getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_VACCINES).document(syncId).delete().await()
    }

    // Medicaciones
    suspend fun syncMedication(hogarSyncId: String, med: PetMedication) {
        val mascotaSyncId = med.mascotaSyncId ?: return
        val syncId = med.syncId ?: return
        getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_MEDICATIONS).document(syncId)
            .set(PetMedicationDto.fromDomain(med)).await()
    }
    fun observeMedications(hogarSyncId: String, mascotaSyncId: String): Flow<List<PetMedication>> = callbackFlow {
        val reg = getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_MEDICATIONS).addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { doc -> 
                doc.toObject(PetMedicationDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }
    suspend fun deleteMedication(hogarSyncId: String, med: PetMedication) {
        val mascotaSyncId = med.mascotaSyncId ?: return
        val syncId = med.syncId ?: return
        getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_MEDICATIONS).document(syncId).delete().await()
    }

    // Desparasitaciones
    suspend fun syncDeworming(hogarSyncId: String, item: PetDeworming) {
        val mascotaSyncId = item.mascotaSyncId ?: return
        val syncId = item.syncId ?: return
        getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_DEWORMINGS).document(syncId)
            .set(PetDewormingDto.fromDomain(item)).await()
    }
    fun observeDewormings(hogarSyncId: String, mascotaSyncId: String): Flow<List<PetDeworming>> = callbackFlow {
        val reg = getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_DEWORMINGS).addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { doc -> 
                doc.toObject(PetDewormingDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }
    suspend fun deleteDeworming(hogarSyncId: String, item: PetDeworming) {
        val mascotaSyncId = item.mascotaSyncId ?: return
        val syncId = item.syncId ?: return
        getPetCollection(hogarSyncId, mascotaSyncId, FirestoreConstants.COL_DEWORMINGS).document(syncId).delete().await()
    }
}
