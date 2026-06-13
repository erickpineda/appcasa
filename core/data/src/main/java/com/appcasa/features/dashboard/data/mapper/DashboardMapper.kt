package com.appcasa.features.dashboard.data.mapper

import com.appcasa.core.domain.model.DashboardConfig
import com.appcasa.core.domain.model.PostIt
import com.appcasa.features.dashboard.data.local.DashboardConfigEntity
import com.appcasa.features.dashboard.data.local.PostItEntity

fun PostItEntity.toDomain(): PostIt {
    return PostIt(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        contenido = contenido,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun PostIt.toEntity(): PostItEntity {
    return PostItEntity(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        contenido = contenido,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun DashboardConfigEntity.toDomain(): DashboardConfig {
    return DashboardConfig(
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        ordenModulos = ordenModulos,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun DashboardConfig.toEntity(): DashboardConfigEntity {
    return DashboardConfigEntity(
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        ordenModulos = ordenModulos,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
