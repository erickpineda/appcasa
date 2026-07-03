package com.appcasa.core.domain.usecase.config

import com.appcasa.core.domain.model.Configuration
import com.appcasa.core.domain.repository.ConfigurationRepository
import javax.inject.Inject

class UpdateConfigurationUseCase @Inject constructor(
    private val repository: ConfigurationRepository
) {
    suspend operator fun invoke(hogarId: String, clave: String, valor: String) {
        repository.upsertConfiguracion(Configuration(hogarId = hogarId, clave = clave, valor = valor))
    }
}
