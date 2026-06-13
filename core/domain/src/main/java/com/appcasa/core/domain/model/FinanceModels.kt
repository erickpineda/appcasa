package com.appcasa.core.domain.model

data class Expense(
    val id: Long = 0,
    val syncId: String? = null,
    val hogarId: Long,
    val hogarSyncId: String? = null,
    val concepto: String,
    val importe: Double,
    val categoria: String,
    val fecha: Long = System.currentTimeMillis(),
    val fotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdById: Long? = null,
    val createdBySyncId: String? = null,
    val archived: Boolean = false,
    val lastSyncedAt: Long? = null
)
