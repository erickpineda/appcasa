package com.appcasa.features.tasks.data.mapper

import com.appcasa.core.domain.model.*
import com.appcasa.features.tasks.data.local.*

fun TareaEntity.toDomain(): Task {
    return Task(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        titulo = titulo,
        descripcion = descripcion,
        prioridad = try { Prioridad.valueOf(prioridad) } catch (e: Exception) { Prioridad.MEDIA },
        tipoContenido = try { TipoContenidoTarea.valueOf(tipoContenido) } catch (e: Exception) { TipoContenidoTarea.LISTA },
        estado = try { EstadoTarea.valueOf(estado) } catch (e: Exception) { EstadoTarea.PENDIENTE },
        categoriaId = categoriaId,
        categoriaSyncId = categoriaSyncId,
        fechaLimite = fechaLimite,
        periodicidad = try { Periodicidad.valueOf(periodicidad) } catch (e: Exception) { Periodicidad.NINGUNA },
        esPersonal = esPersonal,
        completadoEn = completadoEn,
        fotoUri = fotoUri,
        anticipacionMins = anticipacionMins,
        createdAt = createdAt,
        updatedAt = updatedAt,
        points = points,
        puntosOtorgados = puntosOtorgados,
        createdById = createdById,
        createdBySyncId = createdBySyncId,
        archived = archived,
        lastSyncedAt = lastSyncedAt
    )
}

fun Task.toEntity(): TareaEntity {
    return TareaEntity(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        titulo = titulo,
        descripcion = descripcion,
        prioridad = prioridad.name,
        tipoContenido = tipoContenido.name,
        estado = estado.name,
        categoriaId = categoriaId,
        categoriaSyncId = categoriaSyncId,
        fechaLimite = fechaLimite,
        periodicidad = periodicidad.name,
        esPersonal = esPersonal,
        completadoEn = completadoEn,
        fotoUri = fotoUri,
        anticipacionMins = anticipacionMins,
        createdAt = createdAt,
        updatedAt = updatedAt,
        points = points,
        puntosOtorgados = puntosOtorgados,
        createdById = createdById,
        createdBySyncId = createdBySyncId,
        archived = archived,
        lastSyncedAt = lastSyncedAt
    )
}

fun RecompensaEntity.toDomain(): Reward {
    return Reward(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        titulo = titulo,
        descripcion = descripcion,
        costePuntos = costePuntos,
        icono = icono,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun Reward.toEntity(): RecompensaEntity {
    return RecompensaEntity(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        titulo = titulo,
        descripcion = descripcion,
        costePuntos = costePuntos,
        icono = icono,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun CategoriaTareaEntity.toDomain(): TaskCategory {
    return TaskCategory(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        nombre = nombre,
        colorHex = colorHex,
        icono = icono,
        orden = orden,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun TaskCategory.toEntity(): CategoriaTareaEntity {
    return CategoriaTareaEntity(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        nombre = nombre,
        colorHex = colorHex,
        icono = icono,
        orden = orden,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun TareaAsignacionEntity.toDomain(): TaskAssignment {
    return TaskAssignment(
        syncId = syncId,
        tareaId = tareaId,
        tareaSyncId = tareaSyncId,
        miembroId = miembroId,
        miembroSyncId = miembroSyncId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun TaskAssignment.toEntity(): TareaAsignacionEntity {
    return TareaAsignacionEntity(
        syncId = syncId,
        tareaId = tareaId,
        tareaSyncId = tareaSyncId,
        miembroId = miembroId,
        miembroSyncId = miembroSyncId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun TareaCheckItemEntity.toDomain(): TaskCheckItem {
    return TaskCheckItem(
        id = id,
        syncId = syncId,
        tareaId = tareaId,
        tareaSyncId = tareaSyncId,
        texto = texto,
        completado = completado,
        orden = orden,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun TaskCheckItem.toEntity(): TareaCheckItemEntity {
    return TareaCheckItemEntity(
        id = id,
        syncId = syncId,
        tareaId = tareaId,
        tareaSyncId = tareaSyncId,
        texto = texto,
        completado = completado,
        orden = orden,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
