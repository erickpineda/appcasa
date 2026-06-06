package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.Task
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Periodicidad

data class TaskDto(
    val id: Long = 0,
    val hogarId: Long = 0,
    val titulo: String = "",
    val descripcion: String? = null,
    val prioridad: String = "MEDIA",
    val tipoContenido: String = "LISTA",
    val estado: String = "PENDIENTE",
    val categoriaId: Long? = null,
    val fechaLimite: Long? = null,
    val periodicidad: String = "NINGUNA",
    val esPersonal: Boolean = false,
    val completadoEn: Long? = null,
    val fotoUri: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val puntosOtorgados: Boolean = false,
    val createdById: Long? = null,
    val archived: Boolean = false
) {
    fun toDomain(): Task = Task(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        prioridad = Prioridad.valueOf(prioridad),
        tipoContenido = TipoContenidoTarea.valueOf(tipoContenido),
        estado = EstadoTarea.valueOf(estado),
        categoriaId = categoriaId,
        fechaLimite = fechaLimite,
        periodicidad = Periodicidad.valueOf(periodicidad),
        esPersonal = esPersonal,
        completadoEn = completadoEn,
        fotoUri = fotoUri,
        createdAt = createdAt,
        updatedAt = updatedAt,
        puntosOtorgados = puntosOtorgados,
        createdById = createdById,
        archived = archived
    )

    companion object {
        fun fromDomain(task: Task): TaskDto = TaskDto(
            id = task.id,
            hogarId = task.hogarId,
            titulo = task.titulo,
            descripcion = task.descripcion,
            prioridad = task.prioridad.name,
            tipoContenido = task.tipoContenido.name,
            estado = task.estado.name,
            categoriaId = task.categoriaId,
            fechaLimite = task.fechaLimite,
            periodicidad = task.periodicidad.name,
            esPersonal = task.esPersonal,
            completadoEn = task.completadoEn,
            fotoUri = task.fotoUri,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            puntosOtorgados = task.puntosOtorgados,
            createdById = task.createdById,
            archived = task.archived
        )
    }
}
