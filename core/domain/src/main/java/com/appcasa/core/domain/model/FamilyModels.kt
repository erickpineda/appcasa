package com.appcasa.core.domain.model

data class FamilyMember(
    val id: Long = 0,
    val hogarId: Long,
    val nombre: String,
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
    val estadoAnimoUpdatedAt: Long? = null
)
