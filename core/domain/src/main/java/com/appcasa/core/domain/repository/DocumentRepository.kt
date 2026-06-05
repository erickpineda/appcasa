package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getDocumentosByHogar(hogarId: Long): Flow<List<Document>>
    suspend fun insertDocumento(documento: Document): Long
    suspend fun deleteDocumento(documento: Document)
}
