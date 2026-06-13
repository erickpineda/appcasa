package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.*

data class PetWeightDto(
    val syncId: String? = null,
    val mascotaSyncId: String? = null,
    val pesoKg: Double = 0.0,
    val fecha: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain() = PetWeight(
        id = 0,
        syncId = syncId,
        mascotaId = 0,
        mascotaSyncId = mascotaSyncId,
        pesoKg = pesoKg,
        fecha = fecha,
        updatedAt = updatedAt
    )
    companion object {
        fun fromDomain(w: PetWeight) = PetWeightDto(w.syncId, w.mascotaSyncId, w.pesoKg, w.fecha, w.updatedAt)
    }
}

data class PetVaccineDto(
    val syncId: String? = null,
    val mascotaSyncId: String? = null,
    val nombre: String = "",
    val fechaAplicacion: Long = 0,
    val proximaDosis: Long? = null,
    val updatedAt: Long = 0
) {
    fun toDomain() = PetVaccine(
        id = 0,
        syncId = syncId,
        mascotaId = 0,
        mascotaSyncId = mascotaSyncId,
        nombre = nombre,
        fechaAplicacion = fechaAplicacion,
        proximaDosis = proximaDosis,
        updatedAt = updatedAt
    )
    companion object {
        fun fromDomain(v: PetVaccine) = PetVaccineDto(v.syncId, v.mascotaSyncId, v.nombre, v.fechaAplicacion, v.proximaDosis, v.updatedAt)
    }
}

data class PetMedicationDto(
    val syncId: String? = null,
    val mascotaSyncId: String? = null,
    val nombre: String = "",
    val dosis: String = "",
    val frecuencia: String = "",
    val fechaInicio: Long = 0,
    val fechaFin: Long? = null,
    val activa: Boolean = true,
    val updatedAt: Long = 0
) {
    fun toDomain() = PetMedication(
        id = 0,
        syncId = syncId,
        mascotaId = 0,
        mascotaSyncId = mascotaSyncId,
        nombre = nombre,
        dosis = dosis,
        frecuencia = frecuencia,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        activa = activa,
        updatedAt = updatedAt
    )
    companion object {
        fun fromDomain(m: PetMedication) = PetMedicationDto(m.syncId, m.mascotaSyncId, m.nombre, m.dosis, m.frecuencia, m.fechaInicio, m.fechaFin, m.activa, m.updatedAt)
    }
}

data class PetDewormingDto(
    val syncId: String? = null,
    val mascotaSyncId: String? = null,
    val tipo: String = "",
    val producto: String = "",
    val fechaAplicacion: Long = 0,
    val proximaDosis: Long? = null,
    val updatedAt: Long = 0
) {
    fun toDomain() = PetDeworming(
        id = 0,
        syncId = syncId,
        mascotaId = 0,
        mascotaSyncId = mascotaSyncId,
        tipo = tipo,
        producto = producto,
        fechaAplicacion = fechaAplicacion,
        proximaDosis = proximaDosis,
        updatedAt = updatedAt
    )
    companion object {
        fun fromDomain(d: PetDeworming) = PetDewormingDto(d.syncId, d.mascotaSyncId, d.tipo, d.producto, d.fechaAplicacion, d.proximaDosis, d.updatedAt)
    }
}
