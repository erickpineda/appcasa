package com.appcasa.core.domain.model

data class PostIt(
    val id: String = "",
    val hogarId: String = "",
    val contenido: String = "",
    val colorHex: String = "#FFF9C4",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

data class DashboardConfig(
    val hogarId: String = "",
    val ordenModulos: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
