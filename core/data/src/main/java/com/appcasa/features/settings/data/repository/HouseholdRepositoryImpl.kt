package com.appcasa.features.settings.data.repository

import com.appcasa.core.data.local.AppCasaDatabase
import com.appcasa.core.data.remote.source.HouseholdRemoteDataSource
import com.appcasa.core.domain.model.Household
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.mapper.toDomain
import com.appcasa.features.settings.data.mapper.toEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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

  override suspend fun getHogarByCodigo(code: String): Household? = withContext(Dispatchers.IO) {
    configuracionDao.getHogarByCodigo(code)?.toDomain()
  }

  override suspend fun insertHogar(hogar: Household): Long = withContext(Dispatchers.IO) {
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
        configuracionDao.updateHogarSyncTimestamp(id, System.currentTimeMillis())
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      e.printStackTrace()
    }
    id
  }

  override suspend fun findHouseholdRemotely(code: String): Household? = withContext(Dispatchers.IO) {
    remoteDataSource.getHouseholdByCode(code)
  }

  override suspend fun findHouseholdsByUserEmail(email: String): List<Household> = withContext(Dispatchers.IO) {
    remoteDataSource.findHouseholdsByUserEmail(email)
  }

  override suspend fun findHouseholdsByUserUid(uid: String): List<Household> = withContext(Dispatchers.IO) {
    remoteDataSource.findHouseholdsByUserUid(uid)
  }

  override suspend fun updateCodigoHogar(hogarId: Long, newCode: String) {
    val now = System.currentTimeMillis()
    withContext(Dispatchers.IO) {
      // 1. Actualización local
      configuracionDao.updateCodigoHogar(hogarId, newCode, now)
      
      // 2. Sincronización remota con tiempo límite
      try {
        configuracionDao.getHogarByIdOnce(hogarId)?.let { entity ->
          withTimeoutOrNull(3000) {
            remoteDataSource.syncHousehold(entity.toDomain())
            configuracionDao.updateHogarSyncTimestamp(hogarId, System.currentTimeMillis())
          }
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  override suspend fun updateHogarSyncTimestamp(hogarId: Long, timestamp: Long) = withContext(Dispatchers.IO) {
    configuracionDao.updateHogarSyncTimestamp(hogarId, timestamp)
  }

  override suspend fun deleteHogar(id: Long) = withContext(Dispatchers.IO) {
    configuracionDao.deleteHogar(id)
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
