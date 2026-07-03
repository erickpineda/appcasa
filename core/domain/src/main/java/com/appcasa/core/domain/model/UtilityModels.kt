package com.appcasa.core.domain.model

data class Utility(
    val id: String = "",
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String? = null,
    val icono: String = "",
    val activa: Boolean = true,
    val orden: Int = 0,
    val categoria: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
