package com.appcasa.features.pets.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.source.PetRemoteDataSource
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.PetRepository
import com.appcasa.features.pets.data.local.PetDao
import com.appcasa.features.pets.data.mapper.toDomain
import com.appcasa.features.pets.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
  private val petDao: PetDao,
  private val remoteDataSource: PetRemoteDataSource,
  private val syncScheduler: SyncScheduler
) : PetRepository {

  override fun getPesos(mascotaId: String): Flow<List<PetWeight>> {
    return petDao.getPesos(mascotaId).map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun insertPeso(peso: PetWeight): Long {
    val entity = peso.toEntity()
    petDao.upsertPeso(entity)
    // The interface returns Long, but we use String IDs. 
    // Usually Room insert returns rowID if using Long PK, but with String PK it returns nothing.
    // We return 0 as it's not used by use cases anyway.
    return 0L
  }

  override suspend fun deletePeso(peso: PetWeight) {
    petDao.deletePeso(peso.toEntity())
  }

  override fun getVacunas(mascotaId: String): Flow<List<PetVaccine>> {
    return petDao.getVacunas(mascotaId).map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun insertVacuna(vacuna: PetVaccine): Long {
    petDao.upsertVacuna(vacuna.toEntity())
    return 0L
  }

  override suspend fun deleteVacuna(vacuna: PetVaccine) {
    petDao.deleteVacuna(vacuna.toEntity())
  }

  override fun getMedicacionesActivas(mascotaId: String): Flow<List<PetMedication>> {
    return petDao.getMedicacionesActivas(mascotaId).map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun insertMedicacion(med: PetMedication): Long {
    petDao.upsertMedicacion(med.toEntity())
    return 0L
  }

  override suspend fun deleteMedicacion(med: PetMedication) {
    petDao.deleteMedicacion(med.toEntity())
  }

  override fun getDesparasitaciones(mascotaId: String): Flow<List<PetDeworming>> {
    return petDao.getDesparasitaciones(mascotaId).map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun insertDesparasitacion(item: PetDeworming): Long {
    petDao.upsertDesparasitacion(item.toEntity())
    return 0L
  }

  override suspend fun deleteDesparasitacion(item: PetDeworming) {
    petDao.deleteDesparasitacion(item.toEntity())
  }

  override suspend fun getWeightsToSync(hogarId: String): List<PetWeight> {
    return petDao.getWeightsToSync(hogarId).map { it.toDomain() }
  }

  override suspend fun updateWeightSyncTimestamp(id: String) {
    petDao.updateWeightSyncTimestamp(id, System.currentTimeMillis())
  }

  override suspend fun getVaccinesToSync(hogarId: String): List<PetVaccine> {
    return petDao.getVaccinesToSync(hogarId).map { it.toDomain() }
  }

  override suspend fun updateVaccineSyncTimestamp(id: String) {
    petDao.updateVaccineSyncTimestamp(id, System.currentTimeMillis())
  }

  override suspend fun getMedicationsToSync(hogarId: String): List<PetMedication> {
    return petDao.getMedicationsToSync(hogarId).map { it.toDomain() }
  }

  override suspend fun updateMedicationSyncTimestamp(id: String) {
    petDao.updateMedicationSyncTimestamp(id, System.currentTimeMillis())
  }

  override suspend fun getDewormingsToSync(hogarId: String): List<PetDeworming> {
    return petDao.getDewormingsToSync(hogarId).map { it.toDomain() }
  }

  override suspend fun updateDewormingSyncTimestamp(id: String) {
    petDao.updateDewormingSyncTimestamp(id, System.currentTimeMillis())
  }

  override fun startRemoteSync(hogarId: String, mascotaId: String) {
    // TODO Phase 4
  }
}
