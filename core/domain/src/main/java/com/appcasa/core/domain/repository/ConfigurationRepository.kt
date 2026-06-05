package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Configuration
import kotlinx.coroutines.flow.Flow

interface ConfigurationRepository {
    fun getConfiguracion(hogarId: Long): Flow<List<Configuration>>
    suspend fun insertConfiguracion(config: Configuration)
}
