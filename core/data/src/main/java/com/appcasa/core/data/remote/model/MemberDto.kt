package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.*

data class MemberDto(
    val id: Long = 0,
    val hogarId: Long = 0,
    val nombre: String = "",
    val email: String? = null,
    val tipo: String = "PERSONA",
    val fechaNacimiento: Long? = null,
    val fotoUri: String? = null,
    val raza: String? = null,
    val colorPelaje: String? = null,
    val numeroChip: String? = null,
    val veterinarioNombre: String? = null,
    val veterinarioTelefono: String? = null,
    val notas: String? = null,
    val estado: String = "ACTIVO",
    val rol: String = "COLABORADOR",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val puntos: Int = 0,
    val nivel: Int = 1,
    val estadoAnimo: String? = null,
    val estadoAnimoUpdatedAt: Long? = null,
    val urlNube: String? = null,
    val firebaseUid: String? = null
) {
    fun toDomain(): FamilyMember = FamilyMember(
        id = id,
        hogarId = hogarId,
        nombre = nombre,
        email = email,
        tipo = runCatching { TipoMiembro.valueOf(tipo) }.getOrDefault(TipoMiembro.PERSONA),
        fechaNacimiento = fechaNacimiento,
        fotoUri = fotoUri,
        raza = raza,
        colorPelaje = colorPelaje,
        numeroChip = numeroChip,
        veterinarioNombre = veterinarioNombre,
        veterinarioTelefono = veterinarioTelefono,
        notas = notas,
        estado = runCatching { EstadoGeneral.valueOf(estado) }.getOrDefault(EstadoGeneral.ACTIVO),
        rol = runCatching { RolHogar.valueOf(rol) }.getOrDefault(RolHogar.COLABORADOR),
        createdAt = createdAt,
        updatedAt = updatedAt,
        puntos = puntos,
        nivel = nivel,
        estadoAnimo = estadoAnimo,
        estadoAnimoUpdatedAt = estadoAnimoUpdatedAt,
        urlNube = urlNube,
        firebaseUid = firebaseUid
    )

    companion object {
        fun fromDomain(member: FamilyMember): MemberDto = MemberDto(
            id = member.id,
            hogarId = member.hogarId,
            nombre = member.nombre,
            email = member.email,
            tipo = member.tipo.name,
            fechaNacimiento = member.fechaNacimiento,
            fotoUri = member.fotoUri,
            raza = member.raza,
            colorPelaje = member.colorPelaje,
            numeroChip = member.numeroChip,
            veterinarioNombre = member.veterinarioNombre,
            veterinarioTelefono = member.veterinarioTelefono,
            notas = member.notas,
            estado = member.estado.name,
            rol = member.rol.name,
            createdAt = member.createdAt,
            updatedAt = member.updatedAt,
            puntos = member.puntos,
            nivel = member.nivel,
            estadoAnimo = member.estadoAnimo,
            estadoAnimoUpdatedAt = member.estadoAnimoUpdatedAt,
            urlNube = member.urlNube,
            firebaseUid = member.firebaseUid
        )
    }
}
