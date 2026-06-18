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

    suspend fun syncPostIt(hogarSyncId: String, postIt: PostIt) {
        val syncId = postIt.syncId ?: return
        val dto = PostItDto.fromDomain(postIt).copy(hogarSyncId = hogarSyncId)
        getPostItCollection(hogarSyncId).document(syncId)
            .set(dto).await()
    }

    suspend fun deletePostIt(hogarSyncId: String, postIt: PostIt) {
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

    // Dashboard Config
    private fun getConfigCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection("dashboard_config")

    suspend fun syncConfig(hogarSyncId: String, config: com.appcasa.core.domain.model.DashboardConfig) {
        val dto = com.appcasa.core.data.remote.model.DashboardConfigDto.fromDomain(config).copy(hogarSyncId = hogarSyncId)
        // Solo hay una configuración por hogar, usamos un documento fijo "main_config"
        getConfigCollection(hogarSyncId).document("main_config")
            .set(dto).await()
    }

    fun observeConfig(hogarSyncId: String): Flow<com.appcasa.core.domain.model.DashboardConfig?> = callbackFlow {
        val reg = getConfigCollection(hogarSyncId).document("main_config").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val config = snapshot.toObject(com.appcasa.core.data.remote.model.DashboardConfigDto::class.java)?.toDomain()
                trySend(config)
            } else {
                trySend(null)
            }
        }
        awaitClose { reg.remove() }
    }
}
