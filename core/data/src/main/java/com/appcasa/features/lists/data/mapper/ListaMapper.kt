package com.appcasa.features.lists.data.mapper

import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.model.TipoLista
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.lists.data.local.ListaItemEntity

fun ListaEntity.toDomain(): Lista {
    return Lista(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        nombre = nombre,
        tipo = try { TipoLista.valueOf(tipo) } catch (e: Exception) { TipoLista.PERSONALIZADA },
        completada = completada,
        archived = archived,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun Lista.toEntity(): ListaEntity {
    return ListaEntity(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        nombre = nombre,
        tipo = tipo.name,
        completada = completada,
        archived = archived,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun ListaItemEntity.toDomain(): ListaItem {
    return ListaItem(
        id = id,
        syncId = syncId,
        listaId = listaId,
        listaSyncId = listaSyncId,
        texto = texto,
        cantidad = cantidad,
        completado = completado,
        orden = orden,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun ListaItem.toEntity(): ListaItemEntity {
    return ListaItemEntity(
        id = id,
        syncId = syncId,
        listaId = listaId,
        listaSyncId = listaSyncId,
        texto = texto,
        cantidad = cantidad,
        completado = completado,
        orden = orden,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
