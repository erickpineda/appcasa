package com.appcasa.features.settings.data.mapper

import com.appcasa.core.domain.model.Configuration
import com.appcasa.core.domain.model.TipoConfiguracion
import com.appcasa.features.settings.data.local.ConfiguracionEntity

fun ConfiguracionEntity.toDomain(): Configuration {
    return Configuration(
        id = id,
        hogarId = hogarId,
        clave = clave,
        valor = valor,
        tipo = try { TipoConfiguracion.valueOf(tipo) } catch (e: Exception) { TipoConfiguracion.STRING },
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun Configuration.toEntity(): ConfiguracionEntity {
    return ConfiguracionEntity(
        id = id,
        hogarId = hogarId,
        clave = clave,
        valor = valor,
        tipo = tipo.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
