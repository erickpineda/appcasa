package com.appcasa.core.domain.model

data class PetWeight(
    val id: Long = 0,
    val mascotaId: Long,
    val pesoKg: Double,
    val fecha: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

data class PetVaccine(
    val id: Long = 0,
    val mascotaId: Long,
    val nombre: String,
    val fechaAplicacion: Long = System.currentTimeMillis(),
    val proximaDosis: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

data class PetMedication(
    val id: Long = 0,
    val mascotaId: Long,
    val nombre: String,
    val dosis: String,
    val frecuencia: String,
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaFin: Long? = null,
    val activa: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

data class PetDeworming(
    val id: Long = 0,
    val mascotaId: Long,
    val tipo: String, // Interna, Externa, Ambas
    val producto: String,
    val fechaAplicacion: Long = System.currentTimeMillis(),
    val proximaDosis: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
