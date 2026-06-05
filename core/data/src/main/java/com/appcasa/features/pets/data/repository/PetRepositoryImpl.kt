package com.appcasa.features.pets.data.repository

import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.PetRepository
import com.appcasa.features.pets.data.local.MascotaDao
import com.appcasa.features.pets.data.mapper.toDomain
import com.appcasa.features.pets.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val mascotaDao: MascotaDao
) : PetRepository {

    override fun getPesos(mascotaId: Long): Flow<List<PetWeight>> {
        return mascotaDao.getPesos(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertPeso(peso: PetWeight): Long {
        return mascotaDao.insertPeso(peso.toEntity())
    }

    override suspend fun deletePeso(peso: PetWeight) {
        mascotaDao.deletePeso(peso.toEntity())
    }

    override fun getVacunas(mascotaId: Long): Flow<List<PetVaccine>> {
        return mascotaDao.getVacunas(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertVacuna(vacuna: PetVaccine): Long {
        return mascotaDao.insertVacuna(vacuna.toEntity())
    }

    override suspend fun deleteVacuna(vacuna: PetVaccine) {
        mascotaDao.deleteVacuna(vacuna.toEntity())
    }

    override fun getMedicacionesActivas(mascotaId: Long): Flow<List<PetMedication>> {
        return mascotaDao.getMedicacionesActivas(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMedicacion(med: PetMedication): Long {
        return mascotaDao.insertMedicacion(med.toEntity())
    }

    override suspend fun deleteMedicacion(med: PetMedication) {
        mascotaDao.deleteMedicacion(med.toEntity())
    }

    override fun getDesparasitaciones(mascotaId: Long): Flow<List<PetDeworming>> {
        return mascotaDao.getDesparasitaciones(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertDesparasitacion(item: PetDeworming): Long {
        return mascotaDao.insertDesparasitacion(item.toEntity())
    }

    override suspend fun deleteDesparasitacion(item: PetDeworming) {
        mascotaDao.deleteDesparasitacion(item.toEntity())
    }
}
