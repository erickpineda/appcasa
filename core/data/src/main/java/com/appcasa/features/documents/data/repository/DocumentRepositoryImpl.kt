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

    override fun getDocumentosByHogar(hogarId: Long): Flow<List<Document>> {
        return documentoDao.getDocumentosByHogar(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertDocumento(documento: Document): Long {
        var docToInsert = documento
        if (docToInsert.hogarSyncId == null && docToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(docToInsert.hogarId).first()
            docToInsert = docToInsert.copy(hogarSyncId = hogar?.syncId)
        }
        if (docToInsert.syncId == null) {
            docToInsert = docToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = docToInsert.syncId?.let { documentoDao.getDocumentoBySyncId(it) }
        if (existing != null) {
            docToInsert = docToInsert.copy(id = existing.id)
        }
        val id = documentoDao.insertDocumento(docToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(docToInsert.hogarId)
        return id
    }

    override suspend fun deleteDocumento(documento: Document) {
        documentoDao.deleteDocumento(documento.toEntity())
        try {
            val hogar = householdRepository.getHogarById(documento.hogarId).first()
            val hSyncId = documento.hogarSyncId ?: hogar?.syncId
            if (hSyncId != null) {
                remoteDataSource.deleteDocument(hSyncId, documento)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncScheduler.scheduleSync(documento.hogarId)
    }

    override suspend fun updateDocumentSyncTimestamp(docId: Long) {
        documentoDao.updateSyncTimestamp(docId, System.currentTimeMillis())
    }

    override suspend fun downloadDocument(document: Document, localFile: java.io.File): Boolean {
        val hogar = householdRepository.getHogarById(document.hogarId).first()
        val hogarSyncId = hogar?.syncId ?: return false
        val docSyncId = document.syncId ?: return false
        return remoteDataSource.downloadDocument(hogarSyncId, docSyncId, localFile)
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observeDocuments(it) } ?: emptyFlow()
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteDoc ->
                    val existing = remoteDoc.syncId?.let { documentoDao.getDocumentoBySyncId(it) }
                    val hogar = householdRepository.getHogarById(hogarId).first()

                    val docToSave = remoteDoc.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId
                    )

                    if (existing == null || remoteDoc.updatedAt > existing.updatedAt) {
                        documentoDao.insertDocumento(docToSave.toEntity())
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
