package com.appcasa.core.domain.model

data class FamilyMember(
    val id: String = "",
    val hogarId: String = "",
    val firebaseUid: String? = null,
    val email: String? = null,
    val nombre: String = "",
    val tipo: TipoMiembro = TipoMiembro.PERSONA,
    val rol: RolHogar = RolHogar.COLABORADOR,
    val avatarUrl: String? = null,
    val colorHex: String? = null,
    val raza: String? = null,
    val colorPelaje: String? = null,
    val numeroChip: String? = null,
    val veterinarioNombre: String? = null,
    val veterinarioTelefono: String? = null,
    val puntos: Int = 0,
    val nivel: Int = 1,
    val estadoAnimo: String? = null,
    val fechaNacimiento: Long? = null,
    val notas: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String? = null,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
    val lastSyncedAt: Long? = null
)

data class PetSummary(
    val totalCount: Int,
    val typeCounts: Map<TipoMiembro, Int>
)

val FamilyMember.isPet: Boolean
    get() = tipo != TipoMiembro.PERSONA
