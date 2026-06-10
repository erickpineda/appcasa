package com.appcasa.features.pets.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MascotaDao {
  @Query("SELECT * FROM mascota_pesos WHERE mascota_id = :mascotaId ORDER BY fecha DESC")
  fun getPesos(mascotaId: Long): Flow<List<MascotaPesoEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPeso(peso: MascotaPesoEntity): Long

  @Delete
  suspend fun deletePeso(peso: MascotaPesoEntity)

  @Query("SELECT * FROM mascota_vacunas WHERE mascota_id = :mascotaId")
  fun getVacunas(mascotaId: Long): Flow<List<MascotaVacunaEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVacuna(vacuna: MascotaVacunaEntity): Long

  @Delete
  suspend fun deleteVacuna(vacuna: MascotaVacunaEntity)

  @Query("SELECT * FROM mascota_medicaciones WHERE mascota_id = :mascotaId AND activa = 1")
  fun getMedicacionesActivas(mascotaId: Long): Flow<List<MascotaMedicacionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMedicacion(medicacion: MascotaMedicacionEntity): Long

  @Update
  suspend fun updateMedicacion(medicacion: MascotaMedicacionEntity)

  @Delete
  suspend fun deleteMedicacion(medicacion: MascotaMedicacionEntity)

  @Query("SELECT * FROM mascota_pesos WHERE mascota_id = :mascotaId ORDER BY fecha DESC LIMIT 1")
  suspend fun getLatestPeso(mascotaId: Long): MascotaPesoEntity?

  @Query("SELECT * FROM mascota_desparasitaciones WHERE mascota_id = :mascotaId ORDER BY fecha_aplicacion DESC")
  fun getDesparasitaciones(mascotaId: Long): Flow<List<MascotaDesparasitacionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDesparasitacion(item: MascotaDesparasitacionEntity): Long

  @Delete
  suspend fun deleteDesparasitacion(item: MascotaDesparasitacionEntity)

  @Query("SELECT * FROM mascota_pesos WHERE id = :id")
  suspend fun getPesoById(id: Long): MascotaPesoEntity?

  @Query("SELECT * FROM mascota_vacunas WHERE id = :id")
  suspend fun getVacunaById(id: Long): MascotaVacunaEntity?

  @Query("SELECT * FROM mascota_medicaciones WHERE id = :id")
  suspend fun getMedicacionById(id: Long): MascotaMedicacionEntity?

  @Query("SELECT * FROM mascota_desparasitaciones WHERE id = :id")
  suspend fun getDesparasitacionById(id: Long): MascotaDesparasitacionEntity?

  @Query("SELECT * FROM miembros WHERE id = :id")
  suspend fun getMiembroById(id: Long): com.appcasa.features.family.data.local.MiembroEntity?

  // Sync helpers
  @Query("SELECT * FROM mascota_pesos WHERE updated_at > COALESCE(last_synced_at, 0)")
  suspend fun getWeightsToSync(): List<MascotaPesoEntity>

  @Query("UPDATE mascota_pesos SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateWeightSyncTimestamp(id: Long, timestamp: Long)

  @Query("SELECT * FROM mascota_vacunas WHERE updated_at > COALESCE(last_synced_at, 0)")
  suspend fun getVaccinesToSync(): List<MascotaVacunaEntity>

  @Query("UPDATE mascota_vacunas SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateVaccineSyncTimestamp(id: Long, timestamp: Long)

  @Query("SELECT * FROM mascota_medicaciones WHERE updated_at > COALESCE(last_synced_at, 0)")
  suspend fun getMedicationsToSync(): List<MascotaMedicacionEntity>

  @Query("UPDATE mascota_medicaciones SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateMedicationSyncTimestamp(id: Long, timestamp: Long)

  @Query("SELECT * FROM mascota_desparasitaciones WHERE updated_at > COALESCE(last_synced_at, 0)")
  suspend fun getDewormingsToSync(): List<MascotaDesparasitacionEntity>

  @Query("UPDATE mascota_desparasitaciones SET last_synced_at = :timestamp WHERE id = :id")
  suspend fun updateDewormingSyncTimestamp(id: Long, timestamp: Long)
}
