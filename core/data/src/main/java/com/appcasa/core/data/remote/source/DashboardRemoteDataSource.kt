package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.PostItDto
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.PostIt
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getPostItCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection(FirestoreConstants.COL_POSTITS)

    suspend fun syncPostIt(postIt: PostIt) {
        val hogarSyncId = postIt.hogarSyncId ?: return
        val syncId = postIt.syncId ?: return
        getPostItCollection(hogarSyncId).document(syncId)
            .set(PostItDto.fromDomain(postIt)).await()
    }

    suspend fun deletePostIt(postIt: PostIt) {
        val hogarSyncId = postIt.hogarSyncId ?: return
        val syncId = postIt.syncId ?: return
        getPostItCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observePostIts(hogarSyncId: String): Flow<List<PostIt>> = callbackFlow {
        val reg = getPostItCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val postIts = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(PostItDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(postIts)
        }
        awaitClose { reg.remove() }
    }
}
