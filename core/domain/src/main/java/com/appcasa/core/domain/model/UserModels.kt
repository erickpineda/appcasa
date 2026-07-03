package com.appcasa.core.domain.model

data class User(
    val id: String = "",
    val hogarId: String = "",
    val nombre: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val authId: String? = null,
    val rol: RolHogar = RolHogar.COLABORADOR,
    val estado: EstadoGeneral = EstadoGeneral.ACTIVO,
    val miembroId: String? = null,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
