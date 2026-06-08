package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.Household

data class HouseholdDto(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    val estado: String = "ACTIVO",
    val codigoHogar: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(): Household = Household(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        estado = runCatching { EstadoGeneral.valueOf(estado) }.getOrDefault(EstadoGeneral.ACTIVO),
        codigoHogar = codigoHogar,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(household: Household): HouseholdDto = HouseholdDto(
            id = household.id,
            nombre = household.nombre,
            descripcion = household.descripcion,
            estado = household.estado.name,
            codigoHogar = household.codigoHogar,
            createdAt = household.createdAt,
            updatedAt = household.updatedAt
        )
    }
}
