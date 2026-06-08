package com.appcasa.features.dashboard.data.mapper

import com.appcasa.core.domain.model.DashboardConfig
import com.appcasa.core.domain.model.PostIt
import com.appcasa.features.dashboard.data.local.DashboardConfigEntity
import com.appcasa.features.dashboard.data.local.PostItEntity

fun PostItEntity.toDomain(): PostIt {
    return PostIt(
        id = id,
        hogarId = hogarId,
        contenido = contenido,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncId = syncId
    )
}

fun PostIt.toEntity(): PostItEntity {
    return PostItEntity(
        id = id,
        hogarId = hogarId,
        contenido = contenido,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncId = syncId
    )
}

fun DashboardConfigEntity.toDomain(): DashboardConfig {
    return DashboardConfig(
        hogarId = hogarId,
        ordenModulos = ordenModulos
    )
}

fun DashboardConfig.toEntity(): DashboardConfigEntity {
    return DashboardConfigEntity(
        hogarId = hogarId,
        ordenModulos = ordenModulos
    )
}
