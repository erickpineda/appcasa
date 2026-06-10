package com.appcasa.core.domain.model

data class Configuration(
    val id: Long = 0,
    val hogarId: Long,
    val clave: String,
    val valor: String,
    val tipo: TipoConfiguracion = TipoConfiguracion.STRING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncId: String? = null,
    val lastSyncedAt: Long? = null
)
