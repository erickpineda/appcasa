package com.appcasa.features.maintenance.data.mapper

import com.appcasa.core.domain.model.MaintenanceEvent
import com.appcasa.features.maintenance.data.local.MaintenanceEntity

fun MaintenanceEntity.toDomain(): MaintenanceEvent {
    return MaintenanceEvent(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        fechaRealizacion = fechaRealizacion,
        proximaRevision = proximaRevision,
        coste = coste,
        archived = archived,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun MaintenanceEvent.toEntity(): MaintenanceEntity {
    return MaintenanceEntity(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        fechaRealizacion = fechaRealizacion,
        proximaRevision = proximaRevision,
        coste = coste,
        archived = archived,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
