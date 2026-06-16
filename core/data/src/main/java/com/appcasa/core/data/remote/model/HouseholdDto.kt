package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.Household

data class HouseholdDto(
    val syncId: String? = null,
    val nombre: String = "",
    val descripcion: String? = null,
    val estado: String = "ACTIVO",
    val codigoHogar: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(): Household = Household(
        id = 0, // Siempre 0 para que Room genere un ID local nuevo
        syncId = syncId,
        nombre = nombre,
        descripcion = descripcion,
        estado = runCatching { EstadoGeneral.valueOf(estado) }.getOrDefault(EstadoGeneral.ACTIVO),
        codigoHogar = codigoHogar,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = System.currentTimeMillis()
    )

    companion object {
        fun fromDomain(household: Household): HouseholdDto = HouseholdDto(
            syncId = household.syncId,
            nombre = household.nombre,
            descripcion = household.descripcion,
            estado = household.estado.name,
            codigoHogar = household.codigoHogar,
            createdAt = household.createdAt,
            updatedAt = household.updatedAt
        )
    }
}
