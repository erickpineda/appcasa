package com.appcasa.core.domain.model

data class Event(
    val id: String = "",
    val hogarId: String = "",
    val titulo: String = "",
    val descripcion: String? = null,
    val tipo: TipoEvento = TipoEvento.OTRO,
    val fecha: Long = 0L,
    val fechaFin: Long? = null,
    val miembroId: String? = null,
    val todoElDia: Boolean = true,
    val repeticionAnual: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

data class NextEventSummary(
    val title: String = "",
    val timestamp: Long = 0L,
    val isBirthday: Boolean = false
)
