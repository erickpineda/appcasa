package com.appcasa.features.calendar.data.mapper

import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.features.calendar.data.local.EventoEntity

fun EventoEntity.toDomain(): Event {
    return Event(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = try { TipoEvento.valueOf(tipo) } catch (e: Exception) { TipoEvento.OTRO },
        fecha = fecha,
        fechaFin = fechaFin,
        miembroId = miembroId,
        todoElDia = todoElDia,
        repeticionAnual = repeticionAnual,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncId = syncId
    )
}

fun Event.toEntity(): EventoEntity {
    return EventoEntity(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        tipo = tipo.name,
        fecha = fecha,
        fechaFin = fechaFin,
        miembroId = miembroId,
        todoElDia = todoElDia,
        repeticionAnual = repeticionAnual,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncId = syncId
    )
}
