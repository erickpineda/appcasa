package com.appcasa.core.domain.model

data class Reminder(
    val id: String = "",
    val hogarId: String = "",
    val titulo: String = "",
    val descripcion: String? = null,
    val fechaHora: Long = 0L,
    val tipoRepeticion: TipoRepeticion = TipoRepeticion.NINGUNA,
    val tareaId: String? = null,
    val miembroId: String? = null,
    val workerId: String? = null,
    val notificado: Boolean = false,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
