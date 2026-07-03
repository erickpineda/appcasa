package com.appcasa.features.pets.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
  // Weights
  @Query("SELECT * FROM mascota_pesos WHERE mascota_id = :mascotaId AND deleted_at IS NULL ORDER BY fecha DESC")
  fun getPesos(mascotaId: String): Flow<List<PetWeightEntity>>

  @Upsert
  suspend fun upsertPeso(peso: PetWeightEntity)

  @Delete
  suspend fun deletePeso(peso: PetWeightEntity)

  // Vaccines
  @Query("SELECT * FROM mascota_vacunas WHERE mascota_id = :mascotaId AND deleted_at IS NULL ORDER BY fecha_aplicacion DESC")
  fun getVacunas(mascotaId: String): Flow<List<PetVaccineEntity>>

  @Upsert
  suspend fun upsertVacuna(vacuna: PetVaccineEntity)

  @Delete
  suspend fun deleteVacuna(vacuna: PetVaccineEntity)

  // Medications
  @Query("SELECT * FROM mascota_medicaciones WHERE mascota_id = :mascotaId AND activa = 1 AND deleted_at IS NULL ORDER BY fecha_inicio DESC")
  fun getMedicacionesActivas(mascotaId: String): Flow<List<PetMedicationEntity>>

  @Upsert
  suspend fun upsertMedicacion(med: PetMedicationEntity)

  @Delete
  suspend fun deleteMedicacion(med: PetMedicationEntity)

  // Dewormings
  @Query("SELECT * FROM mascota_desparasitaciones WHERE mascota_id = :mascotaId AND deleted_at IS NULL ORDER BY fecha_aplicacion DESC")
  fun getDesparasitaciones(mascotaId: String): Flow<List<PetDewormingEntity>>

  @Upsert
  suspend fun upsertDesparasitacion(item: PetDewormingEntity)

  @Delete
  suspend fun deleteDesparasitacion(item: PetDewormingEntity)

  // Sync queries (Weights)
  @Query("SELECT p.* FROM mascota_pesos p JOIN miembros m ON p.mascota_id = m.id WHERE m.hogar_id = :hogarId AND (p.last_synced_at IS NULL OR p.updated_at > p.last_synced_at)")
  suspend fun getWeightsToSync(hogarId: String): List<PetWeightEntity>

  @Query("UPDATE mascota_pesos SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateWeightSyncTimestamp(id: String, timestamp: Long)

  // Sync queries (Vaccines)
  @Query("SELECT v.* FROM mascota_vacunas v JOIN miembros m ON v.mascota_id = m.id WHERE m.hogar_id = :hogarId AND (v.last_synced_at IS NULL OR v.updated_at > v.last_synced_at)")
  suspend fun getVaccinesToSync(hogarId: String): List<PetVaccineEntity>

  @Query("UPDATE mascota_vacunas SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateVaccineSyncTimestamp(id: String, timestamp: Long)

  // Sync queries (Medications)
  @Query("SELECT med.* FROM mascota_medicaciones med JOIN miembros m ON med.mascota_id = m.id WHERE m.hogar_id = :hogarId AND (med.last_synced_at IS NULL OR med.updated_at > med.last_synced_at)")
  suspend fun getMedicationsToSync(hogarId: String): List<PetMedicationEntity>

  @Query("UPDATE mascota_medicaciones SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateMedicationSyncTimestamp(id: String, timestamp: Long)

  // Sync queries (Dewormings)
  @Query("SELECT d.* FROM mascota_desparasitaciones d JOIN miembros m ON d.mascota_id = m.id WHERE m.hogar_id = :hogarId AND (d.last_synced_at IS NULL OR d.updated_at > d.last_synced_at)")
  suspend fun getDewormingsToSync(hogarId: String): List<PetDewormingEntity>

  @Query("UPDATE mascota_desparasitaciones SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateDewormingSyncTimestamp(id: String, timestamp: Long)
}
