package com.appcasa.features.reminders.data.mapper

import com.appcasa.core.domain.model.Reminder
import com.appcasa.core.domain.model.TipoRepeticion
import com.appcasa.features.reminders.data.local.RecordatorioEntity

fun RecordatorioEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        fechaHora = fechaHora,
        tipoRepeticion = try { TipoRepeticion.valueOf(tipoRepeticion) } catch (e: Exception) { TipoRepeticion.NINGUNA },
        tareaId = tareaId,
        miembroId = miembroId,
        workerId = workerId,
        notificado = notificado,
        activo = activo,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun Reminder.toEntity(): RecordatorioEntity {
    return RecordatorioEntity(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        fechaHora = fechaHora,
        tipoRepeticion = tipoRepeticion.name,
        tareaId = tareaId,
        miembroId = miembroId,
        workerId = workerId,
        notificado = notificado,
        activo = activo,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
