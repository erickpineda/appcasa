package com.appcasa.features.family.data.mapper

import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.RolHogar
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.features.family.data.local.MiembroEntity

fun MiembroEntity.toDomain(): FamilyMember {
  return FamilyMember(
    id = id,
    hogarId = hogarId,
    firebaseUid = firebaseUid,
    email = email,
    nombre = nombre,
    tipo = try { TipoMiembro.valueOf(tipo) } catch (e: Exception) { TipoMiembro.PERSONA },
    rol = try { RolHogar.valueOf(rol) } catch (e: Exception) { RolHogar.COLABORADOR },
    avatarUrl = avatarUrl,
    colorHex = colorHex,
    puntos = puntos,
    nivel = nivel,
    estadoAnimo = estadoAnimo,
    fechaNacimiento = fechaNacimiento,
    notas = notas,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
    deletedAt = deletedAt,
    deletedBy = deletedBy,
    lastSyncedAt = lastSyncedAt
  )
}

fun FamilyMember.toEntity(): MiembroEntity {
  return MiembroEntity(
    id = id,
    hogarId = hogarId,
    firebaseUid = firebaseUid,
    email = email,
    nombre = nombre,
    tipo = tipo.name,
    rol = rol.name,
    avatarUrl = avatarUrl,
    colorHex = colorHex,
    puntos = puntos,
    nivel = nivel,
    estadoAnimo = estadoAnimo,
    fechaNacimiento = fechaNacimiento,
    notas = notas,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
    deletedAt = deletedAt,
    deletedBy = deletedBy,
    lastSyncedAt = lastSyncedAt
  )
}
