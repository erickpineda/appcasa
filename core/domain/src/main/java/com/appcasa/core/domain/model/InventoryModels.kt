package com.appcasa.core.domain.model

data class StockItem(
    val id: String = "",
    val hogarId: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val cantidadActual: Double = 0.0,
    val cantidadMinima: Double = 0.0,
    val unidad: String = "",
    val autoComprar: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
