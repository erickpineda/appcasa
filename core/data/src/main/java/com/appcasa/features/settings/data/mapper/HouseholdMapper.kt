package com.appcasa.features.settings.data.mapper

import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.Household
import com.appcasa.features.settings.data.local.HogarEntity

fun HogarEntity.toDomain(): Household {
    return Household(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        estado = try { EstadoGeneral.valueOf(estado) } catch (e: Exception) { EstadoGeneral.ACTIVO },
        codigoHogar = codigoHogar,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun Household.toEntity(): HogarEntity {
    return HogarEntity(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        estado = estado.name,
        codigoHogar = codigoHogar,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
