package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.*

data class ListDto(
    val id: Long = 0,
    val hogarId: Long = 0,
    val nombre: String = "",
    val tipo: String = "PERSONALIZADA",
    val completada: Boolean = false,
    val archived: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain() = Lista(id, hogarId, nombre, runCatching { TipoLista.valueOf(tipo) }.getOrDefault(TipoLista.PERSONALIZADA), completada, archived, createdAt, updatedAt)
    companion object {
        fun fromDomain(l: Lista) = ListDto(l.id, l.hogarId, l.nombre, l.tipo.name, l.completada, l.archived, l.createdAt, l.updatedAt)
    }
}

data class ListItemDto(
    val id: Long = 0,
    val listaId: Long = 0,
    val texto: String = "",
    val cantidad: String? = null,
    val completado: Boolean = false,
    val orden: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain() = ListaItem(id, listaId, texto, cantidad, completado, orden, createdAt, updatedAt)
    companion object {
        fun fromDomain(i: ListaItem) = ListItemDto(i.id, i.listaId, i.texto, i.cantidad, i.completado, i.orden, i.createdAt, i.updatedAt)
    }
}
