package com.appcasa.core.domain.model

data class Document(
    val id: String = "",
    val hogarId: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val uriPdf: String = "",
    val fechaVencimiento: Long? = null,
    val urlNube: String? = null,
    val sincronizado: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
