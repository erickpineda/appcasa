package com.appcasa.core.domain.model

data class ListaItem(
    val id: String = "",
    val listaId: String = "",
    val texto: String = "",
    val cantidad: String? = null,
    val completado: Boolean = false,
    val orden: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
