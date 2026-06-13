package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.model.TipoEvento

data class EventDto(
    val syncId: String? = null,
    val hogarSyncId: String? = null,
    val titulo: String = "",
    val descripcion: String? = null,
    val tipo: String = "OTRO",
    val fecha: Long = 0,
    val fechaFin: Long? = null,
    val miembroSyncId: String? = null,
    val todoElDia: Boolean = true,
    val repeticionAnual: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(): Event = Event(
        id = 0,
        syncId = syncId,
        hogarId = 0,
        hogarSyncId = hogarSyncId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = runCatching { TipoEvento.valueOf(tipo) }.getOrDefault(TipoEvento.OTRO),
        fecha = fecha,
        fechaFin = fechaFin,
        miembroSyncId = miembroSyncId,
        todoElDia = todoElDia,
        repeticionAnual = repeticionAnual,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(event: Event): EventDto = EventDto(
            syncId = event.syncId,
            hogarSyncId = event.hogarSyncId,
            titulo = event.titulo,
            descripcion = event.descripcion,
            tipo = event.tipo.name,
            fecha = event.fecha,
            fechaFin = event.fechaFin,
            miembroSyncId = event.miembroSyncId,
            todoElDia = event.todoElDia,
            repeticionAnual = event.repeticionAnual,
            createdAt = event.createdAt,
            updatedAt = event.updatedAt
        )
    }
}
