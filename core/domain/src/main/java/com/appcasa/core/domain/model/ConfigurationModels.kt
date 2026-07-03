package com.appcasa.core.domain.model

data class Configuration(
    val id: String = "",
    val hogarId: String = "",
    val clave: String = "",
    val valor: String = "",
    val tipo: TipoConfiguracion = TipoConfiguracion.STRING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
