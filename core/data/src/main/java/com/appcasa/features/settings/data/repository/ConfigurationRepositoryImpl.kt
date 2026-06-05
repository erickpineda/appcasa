package com.appcasa.features.settings.data.repository

import com.appcasa.core.domain.model.Configuration
import com.appcasa.core.domain.repository.ConfigurationRepository
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.mapper.toDomain
import com.appcasa.features.settings.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConfigurationRepositoryImpl @Inject constructor(
    private val configuracionDao: ConfiguracionDao
) : ConfigurationRepository {

    override fun getConfiguracion(hogarId: Long): Flow<List<Configuration>> {
        return configuracionDao.getConfiguracion(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertConfiguracion(config: Configuration) {
        configuracionDao.insertConfiguracion(config.toEntity())
    }
}
