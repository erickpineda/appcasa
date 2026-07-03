package com.appcasa.core.domain.model

data class Lista(
    val id: String = "",
    val hogarId: String = "",
    val nombre: String = "",
    val tipo: TipoLista = TipoLista.PERSONALIZADA,
    val completada: Boolean = false,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
