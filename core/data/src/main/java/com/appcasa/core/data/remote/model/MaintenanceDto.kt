package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.MaintenanceEvent

data class MaintenanceDto(
    val id: Long = 0,
    val hogarId: Long = 0,
    val titulo: String = "",
    val descripcion: String? = null,
    val categoria: String = "",
    val fechaRealizacion: Long = 0,
    val proximaRevision: Long? = null,
    val coste: Double? = null,
    val archived: Boolean = false,
    val updatedAt: Long = 0
) {
    fun toDomain(): MaintenanceEvent = MaintenanceEvent(
        id = id,
        hogarId = hogarId,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        fechaRealizacion = fechaRealizacion,
        proximaRevision = proximaRevision,
        coste = coste,
        archived = archived,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(event: MaintenanceEvent): MaintenanceDto = MaintenanceDto(
            id = event.id,
            hogarId = event.hogarId,
            titulo = event.titulo,
            descripcion = event.descripcion,
            categoria = event.categoria,
            fechaRealizacion = event.fechaRealizacion,
            proximaRevision = event.proximaRevision,
            coste = event.coste,
            archived = event.archived,
            updatedAt = event.updatedAt
        )
    }
}
