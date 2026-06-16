package com.appcasa.features.settings.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.domain.repository.SettingsRepository
import com.appcasa.features.settings.data.local.ConfiguracionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val configuracionDao: ConfiguracionDao,
    private val syncScheduler: SyncScheduler
) : SettingsRepository {

    override fun isCompactView(hogarId: Long): Flow<Boolean> {
        return configuracionDao.getConfiguracion(hogarId)
            .map { list -> list.find { it.clave == "vista_compacta" }?.valor == "true" }
    }

    override fun getCurrencySymbol(hogarId: Long): Flow<String> {
        return configuracionDao.getConfiguracion(hogarId)
            .map { list -> list.find { it.clave == "moneda" }?.valor ?: "€" }
    }

    override suspend fun triggerManualSync(hogarId: Long) {
        syncScheduler.scheduleSync(hogarId)
    }

    override suspend fun exportData(hogarId: Long): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val json = org.json.JSONObject()
                json.put("hogarId", hogarId)
                json.put("timestamp", System.currentTimeMillis())

                val cursorHogar = configuracionDao.getHogarById(hogarId).firstOrNull()
                json.put("hogar", cursorHogar?.nombre ?: "Desconocido")

                // Aquí podemos expandir para exportar otras tablas usando la base de datos inyectada.
                // Por ahora, exportaremos una estructura base representativa.
                val configArray = org.json.JSONArray()
                val configs = configuracionDao.getConfiguracion(hogarId).firstOrNull()
                configs?.forEach { 
                    val item = org.json.JSONObject()
                    item.put("clave", it.clave)
                    item.put("valor", it.valor)
                    configArray.put(item)
                }
                json.put("configuraciones", configArray)
                
                json.toString(4)
            } catch (e: Exception) {
                "{ \"error\": \"Fallo al exportar datos: ${e.message}\" }"
            }
        }
    }
}
