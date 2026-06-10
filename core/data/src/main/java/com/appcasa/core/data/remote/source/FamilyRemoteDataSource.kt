package com.appcasa.core.data.remote.source

import com.appcasa.core.data.remote.model.MemberDto
import com.appcasa.core.domain.model.FamilyMember
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getMemberCollection(hogarId: Long) = 
        firestore.collection("households").document(hogarId.toString()).collection("members")

    suspend fun syncMember(member: FamilyMember) {
        getMemberCollection(member.hogarId).document(member.id.toString())
            .set(MemberDto.fromDomain(member)).await()
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
