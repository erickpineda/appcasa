package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.model.TipoEvento

data class EventDto(
    val id: Long = 0,
    val hogarId: Long = 0,
    val titulo: String = "",
    val descripcion: String? = null,
    val tipo: String = "OTRO",
    val fecha: Long = 0,
    val fechaFin: Long? = null,
    val miembroId: Long? = null,
    val todoElDia: Boolean = true,
    val repeticionAnual: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val syncId: String? = null
) {
    fun toDomain(): Event = Event(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = TipoEvento.valueOf(tipo),
        fecha = fecha,
        fechaFin = fechaFin,
        miembroId = miembroId,
        todoElDia = todoElDia,
        repeticionAnual = repeticionAnual,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncId = syncId
    )

    companion object {
        fun fromDomain(event: Event): EventDto = EventDto(
            id = event.id,
            hogarId = event.hogarId,
            titulo = event.titulo,
            descripcion = event.descripcion,
            tipo = event.tipo.name,
            fecha = event.fecha,
            fechaFin = event.fechaFin,
            miembroId = event.miembroId,
            todoElDia = event.todoElDia,
            repeticionAnual = event.repeticionAnual,
            createdAt = event.createdAt,
            updatedAt = event.updatedAt,
            syncId = event.syncId
        )
    }
}
