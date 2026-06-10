package com.appcasa.features.settings.data.repository

import com.appcasa.core.data.remote.source.HouseholdRemoteDataSource
import com.appcasa.core.domain.model.Household
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.mapper.toDomain
import com.appcasa.features.settings.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HouseholdRepositoryImpl @Inject constructor(
    private val configuracionDao: ConfiguracionDao,
    private val remoteDataSource: HouseholdRemoteDataSource
) : HouseholdRepository {

    override fun getHogarActual(): Flow<Household?> {
        return configuracionDao.getHogarActual().map { entity ->
            entity?.toDomain()
        }
    }

    override fun getAllHogares(): Flow<List<Household>> {
        return configuracionDao.getAllHogares().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getHogarByCodigo(code: String): Household? {
        return configuracionDao.getHogarByCodigo(code)?.toDomain()
    }

    override suspend fun insertHogar(hogar: Household): Long {
        val id = configuracionDao.insertHogar(hogar.toEntity())
        remoteDataSource.syncHousehold(hogar.copy(id = id))
        return id
    }

    override suspend fun findHouseholdRemotely(code: String): Household? {
        return remoteDataSource.getHouseholdByCode(code)
    }

    override suspend fun updateCodigoHogar(hogarId: Long, newCode: String) {
        configuracionDao.updateCodigoHogar(hogarId, newCode)
    }

    override suspend fun deleteAllHogares() {
        configuracionDao.deleteAllHogares()
    }
}
