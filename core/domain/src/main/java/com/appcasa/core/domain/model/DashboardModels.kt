package com.appcasa.core.domain.model

data class PostIt(
    val id: Long = 0,
    val hogarId: Long,
    val contenido: String,
    val colorHex: String = "#FFF9C4",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncId: String? = null,
    val lastSyncedAt: Long? = null
)

data class DashboardConfig(
    val hogarId: Long,
    val ordenModulos: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
