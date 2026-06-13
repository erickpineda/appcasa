package com.appcasa.core.domain.model

data class StockItem(
    val id: Long = 0,
    val syncId: String? = null,
    val hogarId: Long,
    val hogarSyncId: String? = null,
    val nombre: String,
    val categoria: String,
    val cantidadActual: Double,
    val cantidadMinima: Double,
    val unidad: String,
    val autoComprar: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
