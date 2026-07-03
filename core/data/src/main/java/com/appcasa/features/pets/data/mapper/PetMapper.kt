package com.appcasa.features.pets.data.mapper

import com.appcasa.core.domain.model.*
import com.appcasa.features.pets.data.local.*

fun PetWeightEntity.toDomain() = PetWeight(
  id = id,
  mascotaId = mascotaId,
  pesoKg = pesoKg,
  fecha = fecha,
  updatedAt = updatedAt,
  lastSyncedAt = lastSyncedAt
)

fun PetWeight.toEntity() = PetWeightEntity(
  id = if (id.isBlank()) java.util.UUID.randomUUID().toString() else id,
  mascotaId = mascotaId,
  pesoKg = pesoKg,
  fecha = fecha,
  updatedAt = updatedAt,
  lastSyncedAt = lastSyncedAt
)

fun PetVaccineEntity.toDomain() = PetVaccine(
  id = id,
  mascotaId = mascotaId,
  nombre = nombre,
  fechaAplicacion = fechaAplicacion,
  proximaDosis = proximaDosis,
  updatedAt = updatedAt,
  lastSyncedAt = lastSyncedAt
)

fun PetVaccine.toEntity() = PetVaccineEntity(
  id = if (id.isBlank()) java.util.UUID.randomUUID().toString() else id,
  mascotaId = mascotaId,
  nombre = nombre,
  fechaAplicacion = fechaAplicacion,
  proximaDosis = proximaDosis,
  updatedAt = updatedAt,
  lastSyncedAt = lastSyncedAt
)

fun PetMedicationEntity.toDomain() = PetMedication(
  id = id,
  mascotaId = mascotaId,
  nombre = nombre,
  dosis = dosis,
  frecuencia = frecuencia,
  fechaInicio = fechaInicio,
  fechaFin = fechaFin,
  activa = activa,
  updatedAt = updatedAt,
  lastSyncedAt = lastSyncedAt
)

fun PetMedication.toEntity() = PetMedicationEntity(
  id = if (id.isBlank()) java.util.UUID.randomUUID().toString() else id,
  mascotaId = mascotaId,
  nombre = nombre,
  dosis = dosis,
  frecuencia = frecuencia,
  fechaInicio = fechaInicio,
  fechaFin = fechaFin,
  activa = activa,
  updatedAt = updatedAt,
  lastSyncedAt = lastSyncedAt
)

fun PetDewormingEntity.toDomain() = PetDeworming(
  id = id,
  mascotaId = mascotaId,
  tipo = tipo,
  producto = producto,
  fechaAplicacion = fechaAplicacion,
  proximaDosis = proximaDosis,
  updatedAt = updatedAt,
  lastSyncedAt = lastSyncedAt
)

fun PetDeworming.toEntity() = PetDewormingEntity(
  id = if (id.isBlank()) java.util.UUID.randomUUID().toString() else id,
  mascotaId = mascotaId,
  tipo = tipo,
  producto = producto,
  fechaAplicacion = fechaAplicacion,
  proximaDosis = proximaDosis,
  updatedAt = updatedAt,
  lastSyncedAt = lastSyncedAt
)
