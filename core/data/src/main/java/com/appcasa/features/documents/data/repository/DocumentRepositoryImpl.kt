package com.appcasa.features.documents.data.repository

import com.appcasa.core.domain.model.Document
import com.appcasa.core.domain.repository.DocumentRepository
import com.appcasa.features.documents.data.local.DocumentoDao
import com.appcasa.features.documents.data.mapper.toDomain
import com.appcasa.features.documents.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentoDao: DocumentoDao
) : DocumentRepository {

    override fun getDocumentosByHogar(hogarId: Long): Flow<List<Document>> {
        return documentoDao.getDocumentosByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertDocumento(documento: Document): Long {
        return documentoDao.insertDocumento(documento.toEntity())
    }

    override suspend fun deleteDocumento(documento: Document) {
        documentoDao.deleteDocumento(documento.toEntity())
    }
}
