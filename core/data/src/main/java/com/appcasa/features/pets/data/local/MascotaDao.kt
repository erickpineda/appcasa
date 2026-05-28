package com.appcasa.features.pets.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MascotaDao {
    @Query("SELECT * FROM mascota_pesos WHERE mascota_id = :mascotaId ORDER BY fecha DESC")
    fun getPesos(mascotaId: Long): Flow<List<MascotaPesoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeso(peso: MascotaPesoEntity): Long

    @Query("SELECT * FROM mascota_vacunas WHERE mascota_id = :mascotaId")
    fun getVacunas(mascotaId: Long): Flow<List<MascotaVacunaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacuna(vacuna: MascotaVacunaEntity): Long

    @Query("SELECT * FROM mascota_medicaciones WHERE mascota_id = :mascotaId AND activa = 1")
    fun getMedicacionesActivas(mascotaId: Long): Flow<List<MascotaMedicacionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicacion(medicacion: MascotaMedicacionEntity): Long

    @Update
    suspend fun updateMedicacion(medicacion: MascotaMedicacionEntity)

    @Query("SELECT * FROM mascota_pesos WHERE mascota_id = :mascotaId ORDER BY fecha DESC LIMIT 1")
    suspend fun getLatestPeso(mascotaId: Long): MascotaPesoEntity?

    @Query("SELECT * FROM mascota_desparasitaciones WHERE mascota_id = :mascotaId ORDER BY fecha_aplicacion DESC")
    fun getDesparasitaciones(mascotaId: Long): Flow<List<MascotaDesparasitacionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesparasitacion(item: MascotaDesparasitacionEntity): Long
}
