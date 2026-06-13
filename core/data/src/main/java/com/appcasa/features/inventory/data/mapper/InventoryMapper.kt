package com.appcasa.features.inventory.data.mapper

import com.appcasa.core.domain.model.StockItem
import com.appcasa.features.inventory.data.local.StockEntity

fun StockEntity.toDomain(): StockItem {
    return StockItem(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        nombre = nombre,
        categoria = categoria,
        cantidadActual = cantidadActual,
        cantidadMinima = cantidadMinima,
        unidad = unidad,
        autoComprar = autoComprar,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun StockItem.toEntity(): StockEntity {
    return StockEntity(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        nombre = nombre,
        categoria = categoria,
        cantidadActual = cantidadActual,
        cantidadMinima = cantidadMinima,
        unidad = unidad,
        autoComprar = autoComprar,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
