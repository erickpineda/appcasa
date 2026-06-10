package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.DocumentDto
import com.appcasa.core.domain.model.Document
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getDocumentCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("documents")

    suspend fun syncDocument(doc: Document) {
        getDocumentCollection(doc.hogarId).document(doc.id.toString())
            .set(DocumentDto.fromDomain(doc)).await()
    }

    suspend fun deleteDocument(doc: Document) {
        getDocumentCollection(doc.hogarId).document(doc.id.toString()).delete().await()
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
