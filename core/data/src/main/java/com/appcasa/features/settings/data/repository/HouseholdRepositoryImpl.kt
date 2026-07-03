package com.appcasa.features.settings.data.repository

import com.appcasa.core.data.local.AppCasaDatabase
import com.appcasa.core.data.remote.source.HouseholdRemoteDataSource
import com.appcasa.core.domain.model.Household
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.mapper.toDomain
import com.appcasa.features.settings.data.mapper.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
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

  override fun getHogarById(id: String): Flow<Household?> {
    return configuracionDao.getHogarById(id).map { it?.toDomain() }
  }

  override fun getAllHogares(): Flow<List<Household>> {
    return configuracionDao.getAllHogares().map { list -> list.map { it.toDomain() } }
  }

  override suspend fun getHogarByCodigo(code: String): Household? = withContext(Dispatchers.IO) {
    configuracionDao.getHogarByCodigo(code)?.toDomain()
  }

  override suspend fun insertHogar(hogar: Household): String = withContext(Dispatchers.IO) {
    val idToUse = if (hogar.id.isBlank()) UUID.randomUUID().toString() else hogar.id
    val hogartoInsert = hogar.copy(id = idToUse)

    configuracionDao.upsertHogar(hogartoInsert.toEntity())
    
    try {
      remoteDataSource.saveHousehold(hogartoInsert)
      configuracionDao.updateHogarSyncTimestamp(idToUse, System.currentTimeMillis())
    } catch (e: Exception) {
      e.printStackTrace()
    }
    
    idToUse
  }

  override suspend fun findHouseholdRemotely(code: String): Household? = withContext(Dispatchers.IO) {
    try {
      remoteDataSource.getHouseholdByCode(code)
    } catch (e: Exception) {
      null
    }
  }

  override suspend fun findHouseholdsByUserEmail(email: String): List<Household> = withContext(Dispatchers.IO) {
    emptyList() 
  }

  override suspend fun findHouseholdsByUserUid(uid: String): List<Household> = withContext(Dispatchers.IO) {
    try {
      remoteDataSource.findHouseholdsByMemberUid(uid)
    } catch (e: Exception) {
      emptyList()
    }
  }

  override suspend fun updateCodigoHogar(hogarId: String, newCode: String) {
    val now = System.currentTimeMillis()
    withContext(Dispatchers.IO) {
      configuracionDao.updateCodigoHogar(hogarId, newCode, now)
      configuracionDao.getHogarByIdOnce(hogarId)?.let { entity ->
        try {
          remoteDataSource.saveHousehold(entity.toDomain())
          configuracionDao.updateHogarSyncTimestamp(hogarId, System.currentTimeMillis())
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  override suspend fun updateHogarSyncTimestamp(hogarId: String, timestamp: Long) = withContext(Dispatchers.IO) {
    configuracionDao.updateHogarSyncTimestamp(hogarId, timestamp)
  }

  override suspend fun deleteHogar(id: String) = withContext(Dispatchers.IO) {
    configuracionDao.deleteHogar(id)
    try {
      remoteDataSource.deleteHousehold(id)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override suspend fun deleteAllHogares() = withContext(Dispatchers.IO) {
    configuracionDao.deleteAllHogares()
  }

  override suspend fun clearAllLocalData() {
    withContext(Dispatchers.IO) {
      database.clearAllTables()
    }
  }
}
