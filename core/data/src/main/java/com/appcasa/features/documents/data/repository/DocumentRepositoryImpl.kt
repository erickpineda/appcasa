package com.appcasa.features.documents.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.DocumentRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.Document
import com.appcasa.core.domain.repository.DocumentRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.features.documents.data.local.DocumentoDao
import com.appcasa.features.documents.data.mapper.toDomain
import com.appcasa.features.documents.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class DocumentRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val documentoDao: DocumentoDao,
    private val householdRepository: HouseholdRepository,
    private val remoteDataSource: DocumentRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
) : DocumentRepository {

    override fun getDocumentosByHogar(hogarId: String): Flow<List<Document>> {
        return documentoDao.getDocumentosByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertDocumento(documento: Document) {
        documentoDao.upsertDocumento(documento.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(documento.hogarId)
    }

    override suspend fun deleteDocumento(documento: Document) {
        documentoDao.deleteDocumento(documento.toEntity())
        syncScheduler.scheduleSync(documento.hogarId)
    }

    override suspend fun updateDocumentSyncTimestamp(docId: String) {
        documentoDao.updateSyncTimestamp(docId, System.currentTimeMillis())
    }

    override suspend fun downloadDocument(document: Document, localFile: java.io.File): Boolean {
        // TODO Phase 4
        return false
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: String) {
        // TODO: Refactor in Phase 4
    }
}
