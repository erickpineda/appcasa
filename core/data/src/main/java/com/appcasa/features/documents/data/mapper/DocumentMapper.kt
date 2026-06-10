package com.appcasa.features.documents.data.mapper

import com.appcasa.core.domain.model.Document
import com.appcasa.features.documents.data.local.DocumentoEntity

fun DocumentoEntity.toDomain(): Document {
    return Document(
        id = id,
        hogarId = hogarId,
        nombre = nombre,
        categoria = categoria,
        uriPdf = uriPdf,
        fechaVencimiento = fechaVencimiento,
        urlNube = urlNube,
        sincronizado = sincronizado,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun Document.toEntity(): DocumentoEntity {
    return DocumentoEntity(
        id = id,
        hogarId = hogarId,
        nombre = nombre,
        categoria = categoria,
        uriPdf = uriPdf,
        fechaVencimiento = fechaVencimiento,
        urlNube = urlNube,
        sincronizado = sincronizado,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}
