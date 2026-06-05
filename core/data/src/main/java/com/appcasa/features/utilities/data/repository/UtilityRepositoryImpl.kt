package com.appcasa.features.utilities.data.repository

import com.appcasa.core.domain.model.Utility
import com.appcasa.core.domain.repository.UtilityRepository
import com.appcasa.features.utilities.data.local.UtilidadDao
import com.appcasa.features.utilities.data.mapper.toDomain
import com.appcasa.features.utilities.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UtilityRepositoryImpl @Inject constructor(
    private val utilidadDao: UtilidadDao
) : UtilityRepository {

    override fun getUtilidades(): Flow<List<Utility>> {
        return utilidadDao.getUtilidades().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertUtilidad(utility: Utility) {
        utilidadDao.insertUtilidad(utility.toEntity())
    }
}
