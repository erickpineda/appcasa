package com.appcasa.features.pets.data.mapper

import com.appcasa.core.domain.model.*
import com.appcasa.features.pets.data.local.*

fun MascotaPesoEntity.toDomain(): PetWeight {
    return PetWeight(
        id = id,
        syncId = syncId,
        mascotaId = mascotaId,
        mascotaSyncId = mascotaSyncId,
        pesoKg = pesoKg,
        fecha = fecha,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun PetWeight.toEntity(): MascotaPesoEntity {
    return MascotaPesoEntity(
        id = id,
        syncId = syncId,
        mascotaId = mascotaId,
        mascotaSyncId = mascotaSyncId,
        pesoKg = pesoKg,
        fecha = fecha,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun MascotaVacunaEntity.toDomain(): PetVaccine {
    return PetVaccine(
        id = id,
        syncId = syncId,
        mascotaId = mascotaId,
        mascotaSyncId = mascotaSyncId,
        nombre = nombre,
        fechaAplicacion = fechaAplicacion,
        proximaDosis = fechaProxima,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun PetVaccine.toEntity(): MascotaVacunaEntity {
    return MascotaVacunaEntity(
        id = id,
        syncId = syncId,
        mascotaId = mascotaId,
        mascotaSyncId = mascotaSyncId,
        nombre = nombre,
        fechaAplicacion = fechaAplicacion,
        fechaProxima = proximaDosis,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun MascotaMedicacionEntity.toDomain(): PetMedication {
    return PetMedication(
        id = id,
        syncId = syncId,
        mascotaId = mascotaId,
        mascotaSyncId = mascotaSyncId,
        nombre = nombre,
        dosis = dosis,
        frecuencia = frecuencia,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        activa = activa,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun PetMedication.toEntity(): MascotaMedicacionEntity {
    return MascotaMedicacionEntity(
        id = id,
        syncId = syncId,
        mascotaId = mascotaId,
        mascotaSyncId = mascotaSyncId,
        nombre = nombre,
        dosis = dosis,
        frecuencia = frecuencia,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        activa = activa,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun MascotaDesparasitacionEntity.toDomain(): PetDeworming {
    return PetDeworming(
        id = id,
        syncId = syncId,
        mascotaId = mascotaId,
        mascotaSyncId = mascotaSyncId,
        tipo = tipo,
        producto = producto ?: "",
        fechaAplicacion = fechaAplicacion,
        proximaDosis = fechaProxima,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun PetDeworming.toEntity(): MascotaDesparasitacionEntity {
    return MascotaDesparasitacionEntity(
        id = id,
        syncId = syncId,
        mascotaId = mascotaId,
        mascotaSyncId = mascotaSyncId,
        tipo = tipo,
        producto = producto,
        fechaAplicacion = fechaAplicacion,
        fechaProxima = proximaDosis,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
