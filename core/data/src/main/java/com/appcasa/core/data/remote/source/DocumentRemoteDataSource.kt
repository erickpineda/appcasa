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
    private fun getDocumentCollection(hogarId: Long) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarId.toString()).collection(FirestoreConstants.COL_DOCUMENTS)

    private fun getStorageRef(hogarId: Long, docId: Long) = 
        storage.reference.child("${FirestoreConstants.COL_HOUSEHOLDS}/$hogarId/${FirestoreConstants.COL_DOCUMENTS}/$docId.pdf")

    suspend fun syncDocument(doc: Document) {
        // 1. Sincronizar metadatos en Firestore
        getDocumentCollection(doc.hogarId).document(doc.id.toString())
            .set(DocumentDto.fromDomain(doc)).await()
            
        // 2. Si hay un archivo local, subirlo a Storage si no está sincronizado
        doc.uriPdf.let { uri ->
            if (uri.startsWith("/") || uri.startsWith("file://")) {
                val file = java.io.File(uri.replace("file://", ""))
                if (file.exists()) {
                    getStorageRef(doc.hogarId, doc.id).putFile(android.net.Uri.fromFile(file)).await()
                }
            }
        }
    }

    suspend fun deleteDocument(doc: Document) {
        getDocumentCollection(doc.hogarId).document(doc.id.toString()).delete().await()
        try {
            getStorageRef(doc.hogarId, doc.id).delete().await()
        } catch (e: Exception) {
            // Ignorar si el archivo no existe en storage
        }
    }
    
    suspend fun downloadDocument(hogarId: Long, docId: Long, localFile: File): Boolean {
        return try {
            getStorageRef(hogarId, docId).getFile(localFile).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun observeDocuments(hogarId: Long): Flow<List<Document>> = callbackFlow {
        val reg = getDocumentCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val docs = snapshot?.documents?.mapNotNull { it.toObject(DocumentDto::class.java)?.toDomain() } ?: emptyList()
            trySend(docs)
        }
        awaitClose { reg.remove() }
    }
}
