package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.*

data class ListDto(
    val syncId: String? = null,
    val hogarSyncId: String? = null,
    val nombre: String = "",
    val tipo: String = "PERSONALIZADA",
    val completada: Boolean = false,
    val archived: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain() = Lista(
        id = 0,
        syncId = syncId,
        hogarId = 0,
        hogarSyncId = hogarSyncId,
        nombre = nombre,
        tipo = runCatching { TipoLista.valueOf(tipo) }.getOrDefault(TipoLista.PERSONALIZADA),
        completada = completada,
        archived = archived,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    companion object {
        fun fromDomain(l: Lista) = ListDto(l.syncId, l.hogarSyncId, l.nombre, l.tipo.name, l.completada, l.archived, l.createdAt, l.updatedAt)
    }
}

data class ListItemDto(
    val syncId: String? = null,
    val listaSyncId: String? = null,
    val texto: String = "",
    val cantidad: String? = null,
    val completado: Boolean = false,
    val orden: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain() = ListaItem(
        id = 0,
        syncId = syncId,
        listaId = 0,
        listaSyncId = listaSyncId,
        texto = texto,
        cantidad = cantidad,
        completado = completado,
        orden = orden,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    companion object {
        fun fromDomain(i: ListaItem) = ListItemDto(i.syncId, i.listaSyncId, i.texto, i.cantidad, i.completado, i.orden, i.createdAt, i.updatedAt)
    }
}
