package com.appcasa.features.documents.data.repository

import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.DocumentRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.Document
import com.appcasa.core.domain.repository.DocumentRepository
import com.appcasa.features.documents.data.local.DocumentoDao
import com.appcasa.features.documents.data.mapper.toDomain
import com.appcasa.features.documents.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val documentoDao: DocumentoDao,
    private val remoteDataSource: DocumentRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
) : DocumentRepository {

    override fun getDocumentosByHogar(hogarId: Long): Flow<List<Document>> {
        return documentoDao.getDocumentosByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertDocumento(documento: Document): Long {
        val id = documentoDao.insertDocumento(documento.toEntity())
        syncScheduler.scheduleSync(documento.hogarId)
        return id
    }

    override suspend fun deleteDocumento(documento: Document) {
        documentoDao.deleteDocumento(documento.toEntity())
        syncScheduler.scheduleSync(documento.hogarId)
    }

    override suspend fun updateDocumentSyncTimestamp(docId: Long) {
        documentoDao.updateSyncTimestamp(docId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    remoteDataSource.observeDocuments(hogarId)
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteDoc ->
                    val localDoc = documentoDao.getDocumentById(remoteDoc.id)
                    if (localDoc == null || remoteDoc.updatedAt > localDoc.updatedAt) {
                        documentoDao.insertDocumento(remoteDoc.toEntity())
                    }
                }
            }
            .launchIn(appScope)
    }
}
