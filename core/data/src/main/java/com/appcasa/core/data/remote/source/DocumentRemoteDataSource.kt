package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.DocumentDto
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.Document
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private fun getDocumentCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection(FirestoreConstants.COL_DOCUMENTS)

    private fun getStorageRef(hogarSyncId: String, docSyncId: String) = 
        storage.reference.child("${FirestoreConstants.COL_HOUSEHOLDS}/$hogarSyncId/${FirestoreConstants.COL_DOCUMENTS}/$docSyncId.pdf")

    suspend fun syncDocument(hogarSyncId: String, doc: Document) {
        val syncId = doc.syncId ?: return
        val dto = DocumentDto.fromDomain(doc).copy(hogarSyncId = hogarSyncId)
        
        // 1. Sincronizar metadatos en Firestore
        getDocumentCollection(hogarSyncId).document(syncId)
            .set(dto).await()
            
        // 2. Si hay un archivo local, subirlo a Storage si no está sincronizado
        doc.uriPdf.let { uri ->
            if (uri.startsWith("/") || uri.startsWith("file://")) {
                val file = java.io.File(uri.replace("file://", ""))
                if (file.exists()) {
                    getStorageRef(hogarSyncId, syncId).putFile(android.net.Uri.fromFile(file)).await()
                }
            }
        }
    }

    suspend fun deleteDocument(hogarSyncId: String, doc: Document) {
        val syncId = doc.syncId ?: return
        getDocumentCollection(hogarSyncId).document(syncId).delete().await()
        try {
            getStorageRef(hogarSyncId, syncId).delete().await()
        } catch (e: Exception) {
            // Ignorar si el archivo no existe en storage
        }
    }
    
    suspend fun downloadDocument(hogarSyncId: String, docSyncId: String, localFile: File): Boolean {
        return try {
            getStorageRef(hogarSyncId, docSyncId).getFile(localFile).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun observeDocuments(hogarSyncId: String): Flow<List<Document>> = callbackFlow {
        val reg = getDocumentCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val docs = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(DocumentDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(docs)
        }
        awaitClose { reg.remove() }
    }
}
