package com.appcasa.core.data.remote.model

import com.appcasa.core.domain.model.Document

data class DocumentDto(
    val id: Long = 0,
    val hogarId: Long = 0,
    val nombre: String = "",
    val categoria: String = "",
    val uriPdf: String = "",
    val fechaVencimiento: Long? = null,
    val urlNube: String? = null,
    val sincronizado: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(): Document = Document(
        id = id,
        hogarId = hogarId,
        nombre = nombre,
        categoria = categoria,
        uriPdf = uriPdf,
        fechaVencimiento = fechaVencimiento,
        urlNube = urlNube,
        sincronizado = sincronizado,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(doc: Document): DocumentDto = DocumentDto(
            id = doc.id,
            hogarId = doc.hogarId,
            nombre = doc.nombre,
            categoria = doc.categoria,
            uriPdf = doc.uriPdf,
            fechaVencimiento = doc.fechaVencimiento,
            urlNube = doc.urlNube,
            sincronizado = doc.sincronizado,
            createdAt = doc.createdAt,
            updatedAt = doc.updatedAt
        )
    }
}
