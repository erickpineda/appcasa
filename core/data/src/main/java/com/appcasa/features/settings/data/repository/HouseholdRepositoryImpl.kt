package com.appcasa.features.settings.data.repository

import com.appcasa.core.data.local.AppCasaDatabase
import com.appcasa.core.data.remote.source.HouseholdRemoteDataSource
import com.appcasa.core.domain.model.Household
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.mapper.toDomain
import com.appcasa.features.settings.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class HouseholdRepositoryImpl @Inject constructor(
    private val database: AppCasaDatabase,
    private val configuracionDao: ConfiguracionDao,
    private val remoteDataSource: HouseholdRemoteDataSource
) : HouseholdRepository {

    override fun getHogarActual(): Flow<Household?> {
        return configuracionDao.getHogarActual().map { entity ->
            entity?.toDomain()
        }
    }

    override fun getHogarById(id: Long): Flow<Household?> {
        return configuracionDao.getHogarById(id).map { it?.toDomain() }
    }

    override fun getAllHogares(): Flow<List<Household>> {
        return configuracionDao.getAllHogares().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getHogarByCodigo(code: String): Household? {
        return configuracionDao.getHogarByCodigo(code)?.toDomain()
    }

    override suspend fun insertHogar(hogar: Household): Long {
        // Antes de insertar, comprobamos si ya existe por syncId para no duplicar localmente
        val existingEntity = hogar.syncId?.let { configuracionDao.getHogarBySyncId(it) }

        val hogartoInsert = if (existingEntity != null) {
            hogar.copy(id = existingEntity.id) // Mantenemos el ID local para Room
        } else {
            hogar
        }

        val id = configuracionDao.insertHogar(hogartoInsert.toEntity())
        try {
            // Ponemos un timeout de 3 segundos para que no bloquee la creación local si falla la nube
            val syncedHogar = hogartoInsert.copy(id = id)
            withTimeoutOrNull(3000) {
                remoteDataSource.syncHousehold(syncedHogar)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    override suspend fun findHouseholdRemotely(code: String): Household? {
        return remoteDataSource.getHouseholdByCode(code)
    }

    override suspend fun findHouseholdsByUserEmail(email: String): List<Household> {
        return remoteDataSource.findHouseholdsByUserEmail(email)
    }

    override suspend fun findHouseholdsByUserUid(uid: String): List<Household> {
        return remoteDataSource.findHouseholdsByUserUid(uid)
    }

    override suspend fun updateCodigoHogar(hogarId: Long, newCode: String) {
        configuracionDao.updateCodigoHogar(hogarId, newCode)
    }

    override suspend fun deleteAllHogares() {
        configuracionDao.deleteAllHogares()
    }

    override suspend fun clearAllLocalData() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}
