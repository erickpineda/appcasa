package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.StockItem

data class StockDto(
    val id: Long = 0,
    val hogarId: Long = 0,
    val nombre: String = "",
    val categoria: String = "",
    val cantidadActual: Double = 0.0,
    val cantidadMinima: Double = 0.0,
    val unidad: String = "",
    val autoComprar: Boolean = true,
    val updatedAt: Long = 0
) {
    fun toDomain(): StockItem = StockItem(
        id = id,
        hogarId = hogarId,
        nombre = nombre,
        categoria = categoria,
        cantidadActual = cantidadActual,
        cantidadMinima = cantidadMinima,
        unidad = unidad,
        autoComprar = autoComprar,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(item: StockItem): StockDto = StockDto(
            id = item.id,
            hogarId = item.hogarId,
            nombre = item.nombre,
            categoria = item.categoria,
            cantidadActual = item.cantidadActual,
            cantidadMinima = item.cantidadMinima,
            unidad = item.unidad,
            autoComprar = item.autoComprar,
            updatedAt = item.updatedAt
        )
    }
}
