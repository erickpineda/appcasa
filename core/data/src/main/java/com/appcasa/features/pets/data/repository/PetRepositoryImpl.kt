package com.appcasa.features.pets.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.PetRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.repository.PetRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.features.pets.data.local.MascotaDao
import com.appcasa.features.pets.data.mapper.toDomain
import com.appcasa.features.pets.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class PetRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val mascotaDao: MascotaDao,
    private val householdRepository: HouseholdRepository,
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
        var pesoToInsert = peso
        if (pesoToInsert.mascotaSyncId == null && pesoToInsert.mascotaId > 0) {
            val pet = mascotaDao.getMiembroById(pesoToInsert.mascotaId)
            pesoToInsert = pesoToInsert.copy(mascotaSyncId = pet?.syncId)
        }
        if (pesoToInsert.syncId == null) {
            pesoToInsert = pesoToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = pesoToInsert.syncId?.let { mascotaDao.getPesoBySyncId(it) }
        if (existing != null) {
            pesoToInsert = pesoToInsert.copy(id = existing.id)
        }
        val id = mascotaDao.insertPeso(pesoToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(getHogarId(pesoToInsert.mascotaId))
        return id
    }

    override suspend fun deletePeso(peso: PetWeight) {
        val hogarId = getHogarId(peso.mascotaId)
        mascotaDao.deletePeso(peso.toEntity())
        try {
            val hogar = householdRepository.getHogarById(hogarId).first()
            hogar?.syncId?.let { remoteDataSource.deleteWeight(it, peso) }
        } catch (e: Exception) { e.printStackTrace() }
        syncScheduler.scheduleSync(hogarId)
    }

    override fun getVacunas(mascotaId: Long): Flow<List<PetVaccine>> {
        return mascotaDao.getVacunas(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertVacuna(vacuna: PetVaccine): Long {
        var vacunaToInsert = vacuna
        if (vacunaToInsert.mascotaSyncId == null && vacunaToInsert.mascotaId > 0) {
            val pet = mascotaDao.getMiembroById(vacunaToInsert.mascotaId)
            vacunaToInsert = vacunaToInsert.copy(mascotaSyncId = pet?.syncId)
        }
        if (vacunaToInsert.syncId == null) {
            vacunaToInsert = vacunaToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = vacunaToInsert.syncId?.let { mascotaDao.getVacunaBySyncId(it) }
        if (existing != null) {
            vacunaToInsert = vacunaToInsert.copy(id = existing.id)
        }
        val id = mascotaDao.insertVacuna(vacunaToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(getHogarId(vacunaToInsert.mascotaId))
        return id
    }

    override suspend fun deleteVacuna(vacuna: PetVaccine) {
        val hogarId = getHogarId(vacuna.mascotaId)
        mascotaDao.deleteVacuna(vacuna.toEntity())
        try {
            val hogar = householdRepository.getHogarById(hogarId).first()
            hogar?.syncId?.let { remoteDataSource.deleteVaccine(it, vacuna) }
        } catch (e: Exception) { e.printStackTrace() }
        syncScheduler.scheduleSync(hogarId)
    }

    override fun getMedicacionesActivas(mascotaId: Long): Flow<List<PetMedication>> {
        return mascotaDao.getMedicacionesActivas(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMedicacion(med: PetMedication): Long {
        var medToInsert = med
        if (medToInsert.mascotaSyncId == null && medToInsert.mascotaId > 0) {
            val pet = mascotaDao.getMiembroById(medToInsert.mascotaId)
            medToInsert = medToInsert.copy(mascotaSyncId = pet?.syncId)
        }
        if (medToInsert.syncId == null) {
            medToInsert = medToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = medToInsert.syncId?.let { mascotaDao.getMedicacionBySyncId(it) }
        if (existing != null) {
            medToInsert = medToInsert.copy(id = existing.id)
        }
        val id = mascotaDao.insertMedicacion(medToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(getHogarId(medToInsert.mascotaId))
        return id
    }

    override suspend fun deleteMedicacion(med: PetMedication) {
        val hogarId = getHogarId(med.mascotaId)
        mascotaDao.deleteMedicacion(med.toEntity())
        try {
            val hogar = householdRepository.getHogarById(hogarId).first()
            hogar?.syncId?.let { remoteDataSource.deleteMedication(it, med) }
        } catch (e: Exception) { e.printStackTrace() }
        syncScheduler.scheduleSync(hogarId)
    }

    override fun getDesparasitaciones(mascotaId: Long): Flow<List<PetDeworming>> {
        return mascotaDao.getDesparasitaciones(mascotaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertDesparasitacion(item: PetDeworming): Long {
        var itemToInsert = item
        if (itemToInsert.mascotaSyncId == null && itemToInsert.mascotaId > 0) {
            val pet = mascotaDao.getMiembroById(itemToInsert.mascotaId)
            itemToInsert = itemToInsert.copy(mascotaSyncId = pet?.syncId)
        }
        if (itemToInsert.syncId == null) {
            itemToInsert = itemToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = itemToInsert.syncId?.let { mascotaDao.getDesparasitacionBySyncId(it) }
        if (existing != null) {
            itemToInsert = itemToInsert.copy(id = existing.id)
        }
        val id = mascotaDao.insertDesparasitacion(itemToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(getHogarId(itemToInsert.mascotaId))
        return id
    }

    override suspend fun deleteDesparasitacion(item: PetDeworming) {
        val hogarId = getHogarId(item.mascotaId)
        mascotaDao.deleteDesparasitacion(item.toEntity())
        try {
            val hogar = householdRepository.getHogarById(hogarId).first()
            hogar?.syncId?.let { remoteDataSource.deleteDeworming(it, item) }
        } catch (e: Exception) { e.printStackTrace() }
        syncScheduler.scheduleSync(hogarId)
    }

    override suspend fun getWeightsToSync(hogarId: Long): List<PetWeight> {
        return mascotaDao.getWeightsToSync(hogarId).map { it.toDomain() }
    }

    override suspend fun updateWeightSyncTimestamp(id: Long) {
        mascotaDao.updateWeightSyncTimestamp(id, System.currentTimeMillis())
    }

    override suspend fun getVaccinesToSync(hogarId: Long): List<PetVaccine> {
        return mascotaDao.getVaccinesToSync(hogarId).map { it.toDomain() }
    }

    override suspend fun updateVaccineSyncTimestamp(id: Long) {
        mascotaDao.updateVaccineSyncTimestamp(id, System.currentTimeMillis())
    }

    override suspend fun getMedicationsToSync(hogarId: Long): List<PetMedication> {
        return mascotaDao.getMedicationsToSync(hogarId).map { it.toDomain() }
    }

    override suspend fun updateMedicationSyncTimestamp(id: Long) {
        mascotaDao.updateMedicationSyncTimestamp(id, System.currentTimeMillis())
    }

    override suspend fun getDewormingsToSync(hogarId: Long): List<PetDeworming> {
        return mascotaDao.getDewormingsToSync(hogarId).map { it.toDomain() }
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
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    val pet = mascotaDao.getMiembroById(mascotaId)
                    val hSyncId = hogar?.syncId
                    val pSyncId = pet?.syncId
                    if (hSyncId != null && pSyncId != null) {
                        combine(
                            remoteDataSource.observeWeights(hSyncId, pSyncId),
                            remoteDataSource.observeVaccines(hSyncId, pSyncId),
                            remoteDataSource.observeMedications(hSyncId, pSyncId),
                            remoteDataSource.observeDewormings(hSyncId, pSyncId)
                        ) { weights, vaccines, meds, dewormings ->
                            PetDataUpdate(weights, vaccines, meds, dewormings)
                        }
                    } else emptyFlow()
                } else {
                    emptyFlow()
                }
            }
            .onEach { update ->
                update.weights.forEach { remote ->
                    val existing = remote.syncId?.let { mascotaDao.getPesoBySyncId(it) }
                    val petToSave = remote.copy(
                        id = existing?.id ?: 0L,
                        mascotaId = mascotaId,
                        mascotaSyncId = mascotaDao.getMiembroById(mascotaId)?.syncId
                    )
                    if (existing == null || remote.updatedAt > existing.updatedAt) {
                        mascotaDao.insertPeso(petToSave.toEntity())
                    }
                }
                update.vaccines.forEach { remote ->
                    val existing = remote.syncId?.let { mascotaDao.getVacunaBySyncId(it) }
                    val petToSave = remote.copy(
                        id = existing?.id ?: 0L,
                        mascotaId = mascotaId,
                        mascotaSyncId = mascotaDao.getMiembroById(mascotaId)?.syncId
                    )
                    if (existing == null || remote.updatedAt > existing.updatedAt) {
                        mascotaDao.insertVacuna(petToSave.toEntity())
                    }
                }
                update.meds.forEach { remote ->
                    val existing = remote.syncId?.let { mascotaDao.getMedicacionBySyncId(it) }
                    val petToSave = remote.copy(
                        id = existing?.id ?: 0L,
                        mascotaId = mascotaId,
                        mascotaSyncId = mascotaDao.getMiembroById(mascotaId)?.syncId
                    )
                    if (existing == null || remote.updatedAt > existing.updatedAt) {
                        mascotaDao.insertMedicacion(petToSave.toEntity())
                    }
                }
                update.dewormings.forEach { remote ->
                    val existing = remote.syncId?.let { mascotaDao.getDesparasitacionBySyncId(it) }
                    val petToSave = remote.copy(
                        id = existing?.id ?: 0L,
                        mascotaId = mascotaId,
                        mascotaSyncId = mascotaDao.getMiembroById(mascotaId)?.syncId
                    )
                    if (existing == null || remote.updatedAt > existing.updatedAt) {
                        mascotaDao.insertDesparasitacion(petToSave.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }

    private data class PetDataUpdate(
        val weights: List<PetWeight>,
        val vaccines: List<PetVaccine>,
        val meds: List<PetMedication>,
        val dewormings: List<PetDeworming>
    )
}
