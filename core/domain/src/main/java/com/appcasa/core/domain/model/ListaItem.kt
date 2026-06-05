package com.appcasa.core.domain.model

data class ListaItem(
    val id: Long = 0,
    val listaId: Long,
    val texto: String,
    val cantidad: String? = null,
    val completado: Boolean = false,
    val orden: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
