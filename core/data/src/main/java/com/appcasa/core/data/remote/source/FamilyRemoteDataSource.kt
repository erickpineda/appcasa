package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.MemberDto
import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.FamilyMember
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
class FamilyRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private fun getMemberCollection(hogarSyncId: String) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarSyncId).collection(FirestoreConstants.COL_MEMBERS)

    private fun getStorageRef(hogarSyncId: String, memberSyncId: String) = 
        storage.reference.child("${FirestoreConstants.COL_HOUSEHOLDS}/$hogarSyncId/${FirestoreConstants.COL_MEMBERS}/$memberSyncId.jpg")

    suspend fun syncMember(member: FamilyMember) {
        val hogarSyncId = member.hogarSyncId ?: return
        val syncId = member.syncId ?: return
        var updatedMember = member
        
        // Si hay una foto local, subirla a Storage
        member.fotoUri?.let { uri ->
            if (uri.startsWith("/") || uri.startsWith("file://")) {
                val file = File(uri.replace("file://", ""))
                if (file.exists()) {
                    val ref = getStorageRef(hogarSyncId, syncId)
                    ref.putFile(android.net.Uri.fromFile(file)).await()
                    val downloadUrl = ref.downloadUrl.await().toString()
                    updatedMember = member.copy(urlNube = downloadUrl)
                }
            }
        }

        getMemberCollection(hogarSyncId).document(syncId)
            .set(MemberDto.fromDomain(updatedMember)).await()
    }

    suspend fun deleteMember(member: FamilyMember) {
        val hogarSyncId = member.hogarSyncId ?: return
        val syncId = member.syncId ?: return
        getMemberCollection(hogarSyncId).document(syncId).delete().await()
    }

    fun observeMembers(hogarSyncId: String): Flow<List<FamilyMember>> = callbackFlow {
        val reg = getMemberCollection(hogarSyncId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val members = snapshot?.documents?.mapNotNull { doc -> 
                doc.toObject(MemberDto::class.java)?.copy(syncId = doc.id)?.toDomain() 
            } ?: emptyList()
            trySend(members)
        }
        awaitClose { reg.remove() }
    }
}
