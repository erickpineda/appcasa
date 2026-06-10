package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.*
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getPesos(mascotaId: Long): Flow<List<PetWeight>>
    suspend fun insertPeso(peso: PetWeight): Long
    suspend fun deletePeso(peso: PetWeight)

    fun getVacunas(mascotaId: Long): Flow<List<PetVaccine>>
    suspend fun insertVacuna(vacuna: PetVaccine): Long
    suspend fun deleteVacuna(vacuna: PetVaccine)

    fun getMedicacionesActivas(mascotaId: Long): Flow<List<PetMedication>>
    suspend fun insertMedicacion(med: PetMedication): Long
    suspend fun deleteMedicacion(med: PetMedication)

    fun getDesparasitaciones(mascotaId: Long): Flow<List<PetDeworming>>
    suspend fun insertDesparasitacion(item: PetDeworming): Long
    suspend fun deleteDesparasitacion(item: PetDeworming)

    // Sync methods
    suspend fun getWeightsToSync(): List<PetWeight>
    suspend fun updateWeightSyncTimestamp(id: Long)
    
    suspend fun getVaccinesToSync(): List<PetVaccine>
    suspend fun updateVaccineSyncTimestamp(id: Long)
    
    suspend fun getMedicationsToSync(): List<PetMedication>
    suspend fun updateMedicationSyncTimestamp(id: Long)
    
    suspend fun getDewormingsToSync(): List<PetDeworming>
    suspend fun updateDewormingSyncTimestamp(id: Long)

    fun startRemoteSync(hogarId: Long, mascotaId: Long)
}
