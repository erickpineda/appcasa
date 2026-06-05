package com.appcasa.core.domain.model

data class Reminder(
    val id: Long = 0,
    val hogarId: Long,
    val titulo: String,
    val descripcion: String? = null,
    val fechaHora: Long,
    val tipoRepeticion: TipoRepeticion = TipoRepeticion.NINGUNA,
    val tareaId: Long? = null,
    val miembroId: Long? = null,
    val workerId: String? = null,
    val notificado: Boolean = false,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
