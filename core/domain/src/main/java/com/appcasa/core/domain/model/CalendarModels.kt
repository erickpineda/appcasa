package com.appcasa.core.domain.model

data class Event(
    val id: Long = 0,
    val hogarId: Long,
    val titulo: String,
    val descripcion: String? = null,
    val tipo: TipoEvento = TipoEvento.OTRO,
    val fecha: Long,
    val fechaFin: Long? = null,
    val miembroId: Long? = null,
    val todoElDia: Boolean = true,
    val repeticionAnual: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
