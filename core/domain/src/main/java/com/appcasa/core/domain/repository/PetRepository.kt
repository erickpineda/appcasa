package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.*
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getPesos(mascotaId: String): Flow<List<PetWeight>>
    suspend fun insertPeso(peso: PetWeight): Long
    suspend fun deletePeso(peso: PetWeight)

    fun getVacunas(mascotaId: String): Flow<List<PetVaccine>>
    suspend fun insertVacuna(vacuna: PetVaccine): Long
    suspend fun deleteVacuna(vacuna: PetVaccine)

    fun getMedicacionesActivas(mascotaId: String): Flow<List<PetMedication>>
    suspend fun insertMedicacion(med: PetMedication): Long
    suspend fun deleteMedicacion(med: PetMedication)

    fun getDesparasitaciones(mascotaId: String): Flow<List<PetDeworming>>
    suspend fun insertDesparasitacion(item: PetDeworming): Long
    suspend fun deleteDesparasitacion(item: PetDeworming)

    // Sync methods
    suspend fun getWeightsToSync(hogarId: String): List<PetWeight>
    suspend fun updateWeightSyncTimestamp(id: String)
    
    suspend fun getVaccinesToSync(hogarId: String): List<PetVaccine>
    suspend fun updateVaccineSyncTimestamp(id: String)
    
    suspend fun getMedicationsToSync(hogarId: String): List<PetMedication>
    suspend fun updateMedicationSyncTimestamp(id: String)
    
    suspend fun getDewormingsToSync(hogarId: String): List<PetDeworming>
    suspend fun updateDewormingSyncTimestamp(id: String)

    fun startRemoteSync(hogarId: String, mascotaId: String)
}
