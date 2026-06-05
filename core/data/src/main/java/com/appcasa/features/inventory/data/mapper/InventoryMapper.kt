package com.appcasa.features.inventory.data.mapper

import com.appcasa.core.domain.model.StockItem
import com.appcasa.features.inventory.data.local.StockEntity

fun StockEntity.toDomain(): StockItem {
    return StockItem(
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
}

fun StockItem.toEntity(): StockEntity {
    return StockEntity(
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
}
