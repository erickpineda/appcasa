package com.appcasa.core.data.remote.source

import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.FamilyMember
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRemoteDataSource @Inject constructor(
  private val firestore: FirebaseFirestore
) {

  suspend fun saveMember(member: FamilyMember) {
    if (member.hogarId.isBlank()) return
    
    val docId = member.id
    if (docId.isBlank()) return
    
    firestore.collection(FirestoreConstants.COL_HOUSEHOLDS)
      .document(member.hogarId)
      .collection(FirestoreConstants.COL_MEMBERS)
      .document(docId)
      .set(member)
      .await()
  }

  suspend fun getMembersByHousehold(householdId: String): List<FamilyMember> {
    if (householdId.isBlank()) return emptyList()
    
    val query = firestore.collection(FirestoreConstants.COL_HOUSEHOLDS)
      .document(householdId)
      .collection(FirestoreConstants.COL_MEMBERS)
      .get()
      .await()

    return query.documents.mapNotNull { doc ->
      doc.toObject(FamilyMember::class.java)?.copy(id = doc.id)
    }
  }

  suspend fun deleteMember(hogarId: String, memberId: String) {
    if (hogarId.isBlank() || memberId.isBlank()) return

    firestore.collection(FirestoreConstants.COL_HOUSEHOLDS)
      .document(hogarId)
      .collection(FirestoreConstants.COL_MEMBERS)
      .document(memberId)
      .delete()
      .await()
  }
}
