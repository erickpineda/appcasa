package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.*

data class PetWeightDto(
    val id: Long = 0,
    val mascotaId: Long = 0,
    val pesoKg: Double = 0.0,
    val fecha: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain() = PetWeight(id, mascotaId, pesoKg, fecha, updatedAt)
    companion object {
        fun fromDomain(w: PetWeight) = PetWeightDto(w.id, w.mascotaId, w.pesoKg, w.fecha, w.updatedAt)
    }
}

data class PetVaccineDto(
    val id: Long = 0,
    val mascotaId: Long = 0,
    val nombre: String = "",
    val fechaAplicacion: Long = 0,
    val proximaDosis: Long? = null,
    val updatedAt: Long = 0
) {
    fun toDomain() = PetVaccine(id, mascotaId, nombre, fechaAplicacion, proximaDosis, updatedAt)
    companion object {
        fun fromDomain(v: PetVaccine) = PetVaccineDto(v.id, v.mascotaId, v.nombre, v.fechaAplicacion, v.proximaDosis, v.updatedAt)
    }
}

data class PetMedicationDto(
    val id: Long = 0,
    val mascotaId: Long = 0,
    val nombre: String = "",
    val dosis: String = "",
    val frecuencia: String = "",
    val fechaInicio: Long = 0,
    val fechaFin: Long? = null,
    val activa: Boolean = true,
    val updatedAt: Long = 0
) {
    fun toDomain() = PetMedication(id, mascotaId, nombre, dosis, frecuencia, fechaInicio, fechaFin, activa, updatedAt)
    companion object {
        fun fromDomain(m: PetMedication) = PetMedicationDto(m.id, m.mascotaId, m.nombre, m.dosis, m.frecuencia, m.fechaInicio, m.fechaFin, m.activa, m.updatedAt)
    }
}

data class PetDewormingDto(
    val id: Long = 0,
    val mascotaId: Long = 0,
    val tipo: String = "",
    val producto: String = "",
    val fechaAplicacion: Long = 0,
    val proximaDosis: Long? = null,
    val updatedAt: Long = 0
) {
    fun toDomain() = PetDeworming(id, mascotaId, tipo, producto, fechaAplicacion, proximaDosis, updatedAt)
    companion object {
        fun fromDomain(d: PetDeworming) = PetDewormingDto(d.id, d.mascotaId, d.tipo, d.producto, d.fechaAplicacion, d.proximaDosis, d.updatedAt)
    }
}
