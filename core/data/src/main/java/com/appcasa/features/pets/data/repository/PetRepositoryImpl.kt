package com.appcasa.features.pets.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.PetRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.repository.PetRepository
import com.appcasa.features.pets.data.local.MascotaDao
import com.appcasa.features.pets.data.mapper.toDomain
import com.appcasa.features.pets.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val mascotaDao: MascotaDao,
    private val remoteDataSource: PetRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : PetRepository {

    private suspend fun getHogarId(mascotaId: Long): Long {
        val id = mascotaDao.getMiembroById(mascotaId)?.hogarId 
        return if (id == null || id == 0L) currentHouseholdProvider.getCurrentHouseholdId() else id
    }

    override fun getPesos(mascotaId: Long): Flow<List<PetWeight>> {
        return mascotaDao.getPesos(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertPeso(peso: PetWeight): Long {
        val id = mascotaDao.insertPeso(peso.toEntity())
        syncScheduler.scheduleSync(getHogarId(peso.mascotaId))
        return id
    }

    override suspend fun deletePeso(peso: PetWeight) {
        val hogarId = getHogarId(peso.mascotaId)
        mascotaDao.deletePeso(peso.toEntity())
        try {
            remoteDataSource.deleteWeight(hogarId, peso)
        } catch (e: Exception) { e.printStackTrace() }
        syncScheduler.scheduleSync(hogarId)
    }

    override fun getVacunas(mascotaId: Long): Flow<List<PetVaccine>> {
        return mascotaDao.getVacunas(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertVacuna(vacuna: PetVaccine): Long {
        val id = mascotaDao.insertVacuna(vacuna.toEntity())
        syncScheduler.scheduleSync(getHogarId(vacuna.mascotaId))
        return id
    }

    override suspend fun deleteVacuna(vacuna: PetVaccine) {
        val hogarId = getHogarId(vacuna.mascotaId)
        mascotaDao.deleteVacuna(vacuna.toEntity())
        try {
            remoteDataSource.deleteVaccine(hogarId, vacuna)
        } catch (e: Exception) { e.printStackTrace() }
        syncScheduler.scheduleSync(hogarId)
    }

    override fun getMedicacionesActivas(mascotaId: Long): Flow<List<PetMedication>> {
        return mascotaDao.getMedicacionesActivas(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMedicacion(med: PetMedication): Long {
        val id = mascotaDao.insertMedicacion(med.toEntity())
        syncScheduler.scheduleSync(getHogarId(med.mascotaId))
        return id
    }

    override suspend fun deleteMedicacion(med: PetMedication) {
        val hogarId = getHogarId(med.mascotaId)
        mascotaDao.deleteMedicacion(med.toEntity())
        try {
            remoteDataSource.deleteMedication(hogarId, med)
        } catch (e: Exception) { e.printStackTrace() }
        syncScheduler.scheduleSync(hogarId)
    }

    override fun getDesparasitaciones(mascotaId: Long): Flow<List<PetDeworming>> {
        return mascotaDao.getDesparasitaciones(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertDesparasitacion(item: PetDeworming): Long {
        val id = mascotaDao.insertDesparasitacion(item.toEntity())
        syncScheduler.scheduleSync(getHogarId(item.mascotaId))
        return id
    }

    override suspend fun deleteDesparasitacion(item: PetDeworming) {
        val hogarId = getHogarId(item.mascotaId)
        mascotaDao.deleteDesparasitacion(item.toEntity())
        try {
            remoteDataSource.deleteDeworming(hogarId, item)
        } catch (e: Exception) { e.printStackTrace() }
        syncScheduler.scheduleSync(hogarId)
    }

    override suspend fun getWeightsToSync(): List<PetWeight> {
        return mascotaDao.getWeightsToSync().map { it.toDomain() }
    }

    override suspend fun updateWeightSyncTimestamp(id: Long) {
        mascotaDao.updateWeightSyncTimestamp(id, System.currentTimeMillis())
    }

    override suspend fun getVaccinesToSync(): List<PetVaccine> {
        return mascotaDao.getVaccinesToSync().map { it.toDomain() }
    }

    override suspend fun updateVaccineSyncTimestamp(id: Long) {
        mascotaDao.updateVaccineSyncTimestamp(id, System.currentTimeMillis())
    }

    override suspend fun getMedicationsToSync(): List<PetMedication> {
        return mascotaDao.getMedicationsToSync().map { it.toDomain() }
    }

    override suspend fun updateMedicationSyncTimestamp(id: Long) {
        mascotaDao.updateMedicationSyncTimestamp(id, System.currentTimeMillis())
    }

    override suspend fun getDewormingsToSync(): List<PetDeworming> {
        return mascotaDao.getDewormingsToSync().map { it.toDomain() }
    }

    override suspend fun updateDewormingSyncTimestamp(id: Long) {
        mascotaDao.updateDewormingSyncTimestamp(id, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long, mascotaId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    combine(
                        remoteDataSource.observeWeights(hogarId, mascotaId),
                        remoteDataSource.observeVaccines(hogarId, mascotaId),
                        remoteDataSource.observeMedications(hogarId, mascotaId),
                        remoteDataSource.observeDewormings(hogarId, mascotaId)
                    ) { weights, vaccines, meds, dewormings ->
                        PetDataUpdate(weights, vaccines, meds, dewormings)
                    }
                } else {
                    emptyFlow()
                }
            }
            .onEach { update ->
                update.weights.forEach { remote ->
                    val local = mascotaDao.getPesoById(remote.id)
                    if (local == null || remote.updatedAt > local.updatedAt) {
                        mascotaDao.insertPeso(remote.toEntity())
                    }
                }
                update.vaccines.forEach { remote ->
                    val local = mascotaDao.getVacunaById(remote.id)
                    if (local == null || remote.updatedAt > local.updatedAt) {
                        mascotaDao.insertVacuna(remote.toEntity())
                    }
                }
                update.meds.forEach { remote ->
                    val local = mascotaDao.getMedicacionById(remote.id)
                    if (local == null || remote.updatedAt > local.updatedAt) {
                        mascotaDao.insertMedicacion(remote.toEntity())
                    }
                }
                update.dewormings.forEach { remote ->
                    val local = mascotaDao.getDesparasitacionById(remote.id)
                    if (local == null || remote.updatedAt > local.updatedAt) {
                        mascotaDao.insertDesparasitacion(remote.toEntity())
                    }
                }
            }
            .launchIn(appScope)
    }

    private data class PetDataUpdate(
        val weights: List<PetWeight>,
        val vaccines: List<PetVaccine>,
        val meds: List<PetMedication>,
        val dewormings: List<PetDeworming>
    )
}
