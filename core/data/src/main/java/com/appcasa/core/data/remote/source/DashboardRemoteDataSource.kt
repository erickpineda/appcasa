package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.PostItDto
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
    private fun getPostItCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("postits")

    suspend fun syncPostIt(postIt: PostIt) {
        getPostItCollection(postIt.hogarId).document(postIt.id.toString())
            .set(PostItDto.fromDomain(postIt)).await()
    }

    suspend fun deletePostIt(postIt: PostIt) {
        getPostItCollection(postIt.hogarId).document(postIt.id.toString()).delete().await()
    }

    fun observePostIts(hogarId: Long): Flow<List<PostIt>> = callbackFlow {
        val reg = getPostItCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val postIts = snapshot?.documents?.mapNotNull { it.toObject(PostItDto::class.java)?.toDomain() } ?: emptyList()
            trySend(postIts)
        }
        awaitClose { reg.remove() }
    }
}
