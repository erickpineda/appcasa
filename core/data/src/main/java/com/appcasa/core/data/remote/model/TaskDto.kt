package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.Task
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.TaskAssignment
import com.appcasa.core.domain.model.TaskCheckItem

data class TaskDto(
    val syncId: String? = null,
    val hogarSyncId: String? = null,
    val titulo: String = "",
    val descripcion: String? = null,
    val prioridad: String = "MEDIA",
    val tipoContenido: String = "LISTA",
    val estado: String = "PENDIENTE",
    val categoriaSyncId: String? = null,
    val fechaLimite: Long? = null,
    val periodicidad: String = "NINGUNA",
    val esPersonal: Boolean = false,
    val completadoEn: Long? = null,
    val fotoUri: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val points: Int = 10,
    val puntosOtorgados: Boolean = false,
    val createdBySyncId: String? = null,
    val archived: Boolean = false
) {
    fun toDomain(): Task = Task(
        id = 0, // Resolve localmente
        syncId = syncId,
        hogarId = 0, // Resolve localmente
        hogarSyncId = hogarSyncId,
        titulo = titulo,
        descripcion = descripcion,
        prioridad = runCatching { Prioridad.valueOf(prioridad) }.getOrDefault(Prioridad.MEDIA),
        tipoContenido = runCatching { TipoContenidoTarea.valueOf(tipoContenido) }.getOrDefault(TipoContenidoTarea.LISTA),
        estado = runCatching { EstadoTarea.valueOf(estado) }.getOrDefault(EstadoTarea.PENDIENTE),
        categoriaSyncId = categoriaSyncId,
        fechaLimite = fechaLimite,
        periodicidad = runCatching { Periodicidad.valueOf(periodicidad) }.getOrDefault(Periodicidad.NINGUNA),
        esPersonal = esPersonal,
        completadoEn = completadoEn,
        fotoUri = fotoUri,
        createdAt = createdAt,
        updatedAt = updatedAt,
        points = points,
        puntosOtorgados = puntosOtorgados,
        createdBySyncId = createdBySyncId,
        archived = archived
    )

    companion object {
        fun fromDomain(task: Task): TaskDto = TaskDto(
            syncId = task.syncId,
            hogarSyncId = task.hogarSyncId,
            titulo = task.titulo,
            descripcion = task.descripcion,
            prioridad = task.prioridad.name,
            tipoContenido = task.tipoContenido.name,
            estado = task.estado.name,
            categoriaSyncId = task.categoriaSyncId,
            fechaLimite = task.fechaLimite,
            periodicidad = task.periodicidad.name,
            esPersonal = task.esPersonal,
            completadoEn = task.completadoEn,
            fotoUri = task.fotoUri,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            points = task.points,
            puntosOtorgados = task.puntosOtorgados,
            createdBySyncId = task.createdBySyncId,
            archived = task.archived
        )
    }
}

data class TaskAssignmentDto(
    val syncId: String? = null,
    val tareaSyncId: String? = null,
    val miembroSyncId: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(): TaskAssignment = TaskAssignment(
        syncId = syncId,
        tareaId = 0,
        tareaSyncId = tareaSyncId,
        miembroId = 0,
        miembroSyncId = miembroSyncId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(item: TaskAssignment): TaskAssignmentDto = TaskAssignmentDto(
            syncId = item.syncId,
            tareaSyncId = item.tareaSyncId,
            miembroSyncId = item.miembroSyncId,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt
        )
    }
}

data class TaskCheckItemDto(
    val syncId: String? = null,
    val tareaSyncId: String? = null,
    val texto: String = "",
    val completado: Boolean = false,
    val orden: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(): TaskCheckItem = TaskCheckItem(
        id = 0,
        syncId = syncId,
        tareaId = 0,
        tareaSyncId = tareaSyncId,
        texto = texto,
        completado = completado,
        orden = orden,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(item: TaskCheckItem): TaskCheckItemDto = TaskCheckItemDto(
            syncId = item.syncId,
            tareaSyncId = item.tareaSyncId,
            texto = item.texto,
            completado = item.completado,
            orden = item.orden,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt
        )
    }
}
