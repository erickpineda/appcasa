package com.appcasa.core.data.remote.source

import com.appcasa.core.data.utils.FirestoreConstants
import com.appcasa.core.domain.model.Household
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseholdRemoteDataSource @Inject constructor(
  private val firestore: FirebaseFirestore
) {

  suspend fun saveHousehold(household: Household) {
    if (household.id.isBlank()) return
    
    firestore.collection(FirestoreConstants.COL_HOUSEHOLDS)
      .document(household.id)
      .set(household)
      .await()
  }

  suspend fun getHouseholdByCode(code: String): Household? {
    val query = firestore.collection(FirestoreConstants.COL_HOUSEHOLDS)
      .whereEqualTo("codigoHogar", code)
      .limit(1)
      .get()
      .await()

    val doc = query.documents.firstOrNull() ?: return null
    return doc.toObject(Household::class.java)?.copy(id = doc.id)
  }

  suspend fun findHouseholdsByMemberUid(uid: String): List<Household> {
    val membersQuery = firestore.collectionGroup(FirestoreConstants.COL_MEMBERS)
      .whereEqualTo("firebaseUid", uid)
      .get()
      .await()

    val householdIds = membersQuery.documents.mapNotNull { it.getString("hogarId") }.distinct()
    
    if (householdIds.isEmpty()) return emptyList()

    val households = mutableListOf<Household>()
    for (id in householdIds) {
      val doc = firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(id).get().await()
      doc.toObject(Household::class.java)?.copy(id = doc.id)?.let { households.add(it) }
    }
    return households
  }

  suspend fun deleteHousehold(id: String) {
    if (id.isBlank()) return
    firestore.collection(FirestoreConstants.COL_HOUSEHOLDS).document(id).delete().await()
  }
}
