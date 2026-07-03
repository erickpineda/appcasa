package com.appcasa.core.domain.model

data class Expense(
    val id: String = "",
    val hogarId: String = "",
    val concepto: String = "",
    val importe: Double = 0.0,
    val categoria: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val fotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdById: String? = null,
    val archived: Boolean = false,
    val lastSyncedAt: Long? = null
)
