package com.appcasa.core.domain.model

data class Household(
    val id: Long = 0,
    val nombre: String,
    val descripcion: String? = null,
    val estado: EstadoGeneral = EstadoGeneral.ACTIVO,
    val codigoHogar: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
