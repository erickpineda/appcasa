package com.appcasa.features.settings.data.mapper

import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.RolHogar
import com.appcasa.core.domain.model.User
import com.appcasa.features.settings.data.local.UsuarioEntity

fun UsuarioEntity.toDomain(): User {
    return User(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        nombre = nombre,
        email = email,
        avatarUrl = avatarUrl,
        authId = authId,
        rol = try { RolHogar.valueOf(rol) } catch (e: Exception) { RolHogar.COLABORADOR },
        estado = try { EstadoGeneral.valueOf(estado) } catch (e: Exception) { EstadoGeneral.ACTIVO },
        miembroId = miembroId,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun User.toEntity(): UsuarioEntity {
    return UsuarioEntity(
        id = id,
        syncId = syncId,
        hogarId = hogarId,
        hogarSyncId = hogarSyncId,
        nombre = nombre,
        email = email,
        avatarUrl = avatarUrl,
        authId = authId,
        rol = rol.name,
        estado = estado.name,
        miembroId = miembroId,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
