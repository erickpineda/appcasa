package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getDocumentosByHogar(hogarId: String): Flow<List<Document>>
    suspend fun upsertDocumento(documento: Document)
    suspend fun deleteDocumento(documento: Document)
    suspend fun updateDocumentSyncTimestamp(docId: String)
    suspend fun downloadDocument(document: Document, localFile: java.io.File): Boolean
    fun startRemoteSync(hogarId: String)
}
