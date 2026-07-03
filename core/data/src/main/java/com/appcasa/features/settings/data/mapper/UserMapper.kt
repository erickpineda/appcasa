package com.appcasa.features.settings.data.mapper

import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.RolHogar
import com.appcasa.core.domain.model.User
import com.appcasa.features.settings.data.local.UsuarioEntity

fun UsuarioEntity.toDomain(): User {
    return User(
        id = id,
        hogarId = hogarId ?: "",
        nombre = nombre,
        email = email,
        avatarUrl = avatarUrl,
        authId = authId,
        miembroId = miembroId,
        estado = try { EstadoGeneral.valueOf(estado) } catch (e: Exception) { EstadoGeneral.ACTIVO },
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun User.toEntity(): UsuarioEntity {
  return UsuarioEntity(
    id = if (id.isBlank()) java.util.UUID.randomUUID().toString() else id,
    hogarId = hogarId,
        nombre = nombre,
        email = email,
        avatarUrl = avatarUrl,
        authId = authId,
        miembroId = miembroId,
        estado = estado.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
