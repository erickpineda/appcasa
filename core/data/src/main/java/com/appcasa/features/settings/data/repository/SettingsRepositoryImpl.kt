package com.appcasa.features.settings.data.repository

import com.appcasa.core.domain.repository.SettingsRepository
import com.appcasa.features.settings.data.local.ConfiguracionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val configuracionDao: ConfiguracionDao
) : SettingsRepository {

    override fun isCompactView(hogarId: Long): Flow<Boolean> {
        return configuracionDao.getConfiguracion(hogarId)
            .map { list -> list.find { it.clave == "vista_compacta" }?.valor == "true" }
    }

    override fun getCurrencySymbol(hogarId: Long): Flow<String> {
        return configuracionDao.getConfiguracion(hogarId)
            .map { list -> list.find { it.clave == "moneda" }?.valor ?: "€" }
    }
}
