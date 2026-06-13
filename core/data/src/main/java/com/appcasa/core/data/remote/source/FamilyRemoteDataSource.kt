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
    private fun getMemberCollection(hogarId: Long) = 
        firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(hogarId.toString()).collection(FirestoreConstants.COL_MEMBERS)

    private fun getStorageRef(hogarId: Long, memberId: Long) = 
        storage.reference.child("${FirestoreConstants.COL_HOUSEHOLDS}/$hogarId/${FirestoreConstants.COL_MEMBERS}/$memberId.jpg")

    suspend fun syncMember(member: FamilyMember) {
        var updatedMember = member
        
        // Si hay una foto local, subirla a Storage
        member.fotoUri?.let { uri ->
            if (uri.startsWith("/") || uri.startsWith("file://")) {
                val file = File(uri.replace("file://", ""))
                if (file.exists()) {
                    val ref = getStorageRef(member.hogarId, member.id)
                    ref.putFile(android.net.Uri.fromFile(file)).await()
                    val downloadUrl = ref.downloadUrl.await().toString()
                    updatedMember = member.copy(urlNube = downloadUrl)
                }
            }
        }

        getMemberCollection(member.hogarId).document(member.id.toString())
            .set(MemberDto.fromDomain(updatedMember)).await()
    }

    suspend fun deleteMember(member: FamilyMember) {
        getMemberCollection(member.hogarId).document(member.id.toString()).delete().await()
    }

    fun observeMembers(hogarId: Long): Flow<List<FamilyMember>> = callbackFlow {
        val reg = getMemberCollection(hogarId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val members = snapshot?.documents?.mapNotNull { it.toObject(MemberDto::class.java)?.toDomain() } ?: emptyList()
            trySend(members)
        }
        awaitClose { reg.remove() }
    }
}
