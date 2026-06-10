package com.appcasa.core.domain.model

data class MaintenanceEvent(
    val id: Long = 0,
    val hogarId: Long,
    val titulo: String,
    val descripcion: String? = null,
    val categoria: String,
    val fechaRealizacion: Long = System.currentTimeMillis(),
    val proximaRevision: Long? = null,
    val coste: Double? = null,
    val archived: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
