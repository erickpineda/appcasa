package com.appcasa.features.tasks.data.mapper

import com.appcasa.core.domain.model.*
import com.appcasa.features.tasks.data.local.*

fun TareaEntity.toDomain(): Task {
    return Task(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        prioridad = try { Prioridad.valueOf(prioridad) } catch (e: Exception) { Prioridad.MEDIA },
        tipoContenido = try { TipoContenidoTarea.valueOf(tipoContenido) } catch (e: Exception) { TipoContenidoTarea.LISTA },
        estado = try { EstadoTarea.valueOf(estado) } catch (e: Exception) { EstadoTarea.PENDIENTE },
        categoriaId = categoriaId,
        fechaLimite = fechaLimite,
        periodicidad = try { Periodicidad.valueOf(periodicidad) } catch (e: Exception) { Periodicidad.NINGUNA },
        esPersonal = esPersonal,
        completadoEn = completadoEn,
        fotoUri = fotoUri,
        anticipacionMins = anticipacionMins,
        createdAt = createdAt,
        updatedAt = updatedAt,
        puntosOtorgados = puntosOtorgados,
        createdById = createdById,
        archived = archived
    )
}

fun Task.toEntity(): TareaEntity {
    return TareaEntity(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        prioridad = prioridad.name,
        tipoContenido = tipoContenido.name,
        estado = estado.name,
        categoriaId = categoriaId,
        fechaLimite = fechaLimite,
        periodicidad = periodicidad.name,
        esPersonal = esPersonal,
        completadoEn = completadoEn,
        fotoUri = fotoUri,
        anticipacionMins = anticipacionMins,
        createdAt = createdAt,
        updatedAt = updatedAt,
        puntosOtorgados = puntosOtorgados,
        createdById = createdById,
        archived = archived
    )
}

fun RecompensaEntity.toDomain(): Reward {
    return Reward(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        costoPuntos = costePuntos,
        icono = icono
    )
}

fun Reward.toEntity(): RecompensaEntity {
    return RecompensaEntity(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        costePuntos = costoPuntos,
        icono = icono
    )
}

fun CategoriaTareaEntity.toDomain(): TaskCategory {
    return TaskCategory(
        id = id,
        hogarId = hogarId,
        nombre = nombre,
        colorHex = colorHex,
        icono = icono,
        orden = orden
    )
}

fun TaskCategory.toEntity(): CategoriaTareaEntity {
    return CategoriaTareaEntity(
        id = id,
        hogarId = hogarId,
        nombre = nombre,
        colorHex = colorHex,
        icono = icono,
        orden = orden
    )
}

fun TareaAsignacionEntity.toDomain(): TaskAssignment {
    return TaskAssignment(
        tareaId = tareaId,
        miembroId = miembroId,
        createdAt = createdAt
    )
}

fun TaskAssignment.toEntity(): TareaAsignacionEntity {
    return TareaAsignacionEntity(
        tareaId = tareaId,
        miembroId = miembroId,
        createdAt = createdAt
    )
}

fun TareaCheckItemEntity.toDomain(): TaskCheckItem {
    return TaskCheckItem(
        id = id,
        tareaId = tareaId,
        texto = texto,
        completado = completado,
        orden = orden,
        createdAt = createdAt
    )
}

fun TaskCheckItem.toEntity(): TareaCheckItemEntity {
    return TareaCheckItemEntity(
        id = id,
        tareaId = tareaId,
        texto = texto,
        completado = completado,
        orden = orden,
        createdAt = createdAt
    )
}
