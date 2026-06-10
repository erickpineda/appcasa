package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.*
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
    private fun getPetCollection(hogarId: Long, mascotaId: Long, sub: String) = 
        firestore.collection("households").document(hogarId.toString())
            .collection("members").document(mascotaId.toString())
            .collection(sub)

    // Pesos
    suspend fun syncWeight(hogarId: Long, weight: PetWeight) {
        getPetCollection(hogarId, weight.mascotaId, "weights").document(weight.id.toString())
            .set(PetWeightDto.fromDomain(weight)).await()
    }
    fun observeWeights(hogarId: Long, mascotaId: Long): Flow<List<PetWeight>> = callbackFlow {
        val reg = getPetCollection(hogarId, mascotaId, "weights").addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(PetWeightDto::class.java)?.toDomain() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    // Vacunas
    suspend fun syncVaccine(hogarId: Long, vaccine: PetVaccine) {
        getPetCollection(hogarId, vaccine.mascotaId, "vaccines").document(vaccine.id.toString())
            .set(PetVaccineDto.fromDomain(vaccine)).await()
    }
    fun observeVaccines(hogarId: Long, mascotaId: Long): Flow<List<PetVaccine>> = callbackFlow {
        val reg = getPetCollection(hogarId, mascotaId, "vaccines").addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(PetVaccineDto::class.java)?.toDomain() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    // Medicaciones
    suspend fun syncMedication(hogarId: Long, med: PetMedication) {
        getPetCollection(hogarId, med.mascotaId, "medications").document(med.id.toString())
            .set(PetMedicationDto.fromDomain(med)).await()
    }
    fun observeMedications(hogarId: Long, mascotaId: Long): Flow<List<PetMedication>> = callbackFlow {
        val reg = getPetCollection(hogarId, mascotaId, "medications").addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(PetMedicationDto::class.java)?.toDomain() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    // Desparasitaciones
    suspend fun syncDeworming(hogarId: Long, item: PetDeworming) {
        getPetCollection(hogarId, item.mascotaId, "dewormings").document(item.id.toString())
            .set(PetDewormingDto.fromDomain(item)).await()
    }
    fun observeDewormings(hogarId: Long, mascotaId: Long): Flow<List<PetDeworming>> = callbackFlow {
        val reg = getPetCollection(hogarId, mascotaId, "dewormings").addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(PetDewormingDto::class.java)?.toDomain() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }
}
