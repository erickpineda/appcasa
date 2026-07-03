package com.appcasa.core.domain.usecase.config

import com.appcasa.core.domain.model.Configuration
import com.appcasa.core.domain.repository.ConfigurationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConfigurationUseCase @Inject constructor(
    private val repository: ConfigurationRepository
) {
    operator fun invoke(hogarId: String): Flow<List<Configuration>> {
        return repository.getConfiguracion(hogarId)
    }
}
