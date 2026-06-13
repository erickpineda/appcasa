package com.appcasa.features.family.data.mapper

import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.RolHogar
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.features.family.data.local.MiembroEntity

fun MiembroEntity.toDomain(): FamilyMember {
    return FamilyMember(
        id = id,
        hogarId = hogarId,
        nombre = nombre,
        email = email,
        tipo = try { TipoMiembro.valueOf(tipo) } catch (e: Exception) { TipoMiembro.PERSONA },
        fechaNacimiento = fechaNacimiento,
        fotoUri = fotoUri,
        raza = raza,
        colorPelaje = colorPelaje,
        numeroChip = numeroChip,
        veterinarioNombre = veterinarioNombre,
        veterinarioTelefono = veterinarioTelefono,
        notas = notas,
        estado = try { EstadoGeneral.valueOf(estado) } catch (e: Exception) { EstadoGeneral.ACTIVO },
        rol = try { RolHogar.valueOf(rol) } catch (e: Exception) { RolHogar.COLABORADOR },
        createdAt = createdAt,
        updatedAt = updatedAt,
        puntos = puntos,
        nivel = nivel,
        estadoAnimo = estadoAnimo,
        estadoAnimoUpdatedAt = estadoAnimoUpdatedAt,
        urlNube = urlNube,
        firebaseUid = firebaseUid,
        lastSyncedAt = lastSyncedAt
    )
}

fun FamilyMember.toEntity(): MiembroEntity {
    return MiembroEntity(
        id = id,
        hogarId = hogarId,
        nombre = nombre,
        email = email,
        tipo = tipo.name,
        fechaNacimiento = fechaNacimiento,
        fotoUri = fotoUri,
        raza = raza,
        colorPelaje = colorPelaje,
        numeroChip = numeroChip,
        veterinarioNombre = veterinarioNombre,
        veterinarioTelefono = veterinarioTelefono,
        notas = notas,
        estado = estado.name,
        rol = rol.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        puntos = puntos,
        nivel = nivel,
        estadoAnimo = estadoAnimo,
        estadoAnimoUpdatedAt = estadoAnimoUpdatedAt,
        urlNube = urlNube,
        firebaseUid = firebaseUid,
        lastSyncedAt = lastSyncedAt
    )
}
