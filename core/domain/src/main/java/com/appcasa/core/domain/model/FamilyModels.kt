package com.appcasa.core.domain.model

data class FamilyMember(
    val id: Long = 0,
    val syncId: String? = null,
    val hogarId: Long,
    val hogarSyncId: String? = null,
    val nombre: String,
    val email: String? = null,
    val tipo: TipoMiembro = TipoMiembro.PERSONA,
    val fechaNacimiento: Long? = null,
    val fotoUri: String? = null,
    val raza: String? = null,
    val colorPelaje: String? = null,
    val numeroChip: String? = null,
    val veterinarioNombre: String? = null,
    val veterinarioTelefono: String? = null,
    val notas: String? = null,
    val estado: EstadoGeneral = EstadoGeneral.ACTIVO,
    val rol: RolHogar = RolHogar.COLABORADOR,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val puntos: Int = 0,
    val nivel: Int = 1,
    val estadoAnimo: String? = null,
    val estadoAnimoUpdatedAt: Long? = null,
    val urlNube: String? = null,
    val firebaseUid: String? = null,
    val lastSyncedAt: Long? = null
)

data class PetSummary(
    val totalCount: Int,
    val typeCounts: Map<TipoMiembro, Int>
)

val FamilyMember.isPet: Boolean
    get() = tipo != TipoMiembro.PERSONA
